package vn.edu.crs.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.orderservice.client.ProductClient;
import vn.edu.crs.orderservice.dto.OrderItemRequestDTO;
import vn.edu.crs.orderservice.dto.OrderRequestDTO;
import vn.edu.crs.orderservice.entity.Order;
import vn.edu.crs.orderservice.entity.OrderItem;
import vn.edu.crs.orderservice.entity.OrderStatus;
import vn.edu.crs.orderservice.entity.PaymentStatus;
import vn.edu.crs.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> PAYMENT_METHODS = Set.of("COD", "BANK_TRANSFER");

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    /**
     * Tạo đơn hàng / Checkout.
     *
     * 1. Gộp các dòng trùng productId.
     * 2. Với từng sản phẩm: lấy tên + giá THẬT từ product-service (không tin giá client gửi),
     *    rồi trừ tồn kho (reserve-stock). Hết hàng -> 409.
     * 3. Nếu một sản phẩm lỗi giữa chừng -> HOÀN KHO các sản phẩm đã trừ trước đó (bù trừ),
     *    vì order_db và product_db là 2 database riêng, không có transaction chung.
     * 4. Lưu đơn kèm snapshot tên + giá tại thời điểm đặt.
     */
    @Transactional
    public Order createOrder(Long customerId, OrderRequestDTO dto) {
        if (customerId == null) {
            throw new IllegalArgumentException("Khong xac dinh duoc customer");
        }
        String paymentMethod = dto.getPaymentMethod().trim().toUpperCase();
        if (!PAYMENT_METHODS.contains(paymentMethod)) {
            throw new IllegalArgumentException("Phuong thuc thanh toan khong hop le. Gia tri hop le: " + PAYMENT_METHODS);
        }

        Map<Long, OrderItemRequestDTO> mergedItems = mergeItems(dto.getItems());

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setShippingAddress(dto.getShippingAddress().trim());
        order.setPhone(dto.getPhone().trim());
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);

        // productId -> số lượng ĐÃ trừ kho thành công (để hoàn lại nếu lỗi)
        Map<Long, Integer> reserved = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        try {
            for (OrderItemRequestDTO itemDTO : mergedItems.values()) {
                int quantity = itemDTO.getQuantity();
                String productName;
                BigDecimal price;

                ProductClient.ProductInfo product = productClient.getProduct(itemDTO.getProductId());
                if (product != null) {
                    productName = product.getName();
                    price = product.getPrice();
                    productClient.reserveStock(itemDTO.getProductId(), quantity);
                    reserved.put(itemDTO.getProductId(), quantity);
                } else {
                    // Chỉ khi product-service.validation-enabled=false (test độc lập)
                    productName = itemDTO.getProductName() == null || itemDTO.getProductName().isBlank()
                            ? "Product #" + itemDTO.getProductId()
                            : itemDTO.getProductName();
                    price = itemDTO.getPrice();
                    if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("price cua san pham khong hop le");
                    }
                }

                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(itemDTO.getProductId());
                orderItem.setProductName(productName);
                orderItem.setPrice(price);
                orderItem.setQuantity(quantity);
                orderItem.setSubtotal(subtotal);
                order.addItem(orderItem);
                totalAmount = totalAmount.add(subtotal);
            }

            order.setTotalAmount(totalAmount);
            return orderRepository.saveAndFlush(order);
        } catch (RuntimeException e) {
            releaseAll(reserved, "tao don that bai");
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Customer chỉ xem được đơn của mình (đơn người khác trả 404 - chống IDOR). */
    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId, Long customerId, boolean isAdmin) {
        Order order = findOrder(orderId);
        if (!isAdmin && !order.getCustomerId().equals(customerId)) {
            throw new NoSuchElementException("Khong tim thay don hang id = " + orderId);
        }
        return order;
    }

    /** Customer huỷ đơn khi còn PENDING -> hoàn lại tồn kho. */
    @Transactional
    public Order cancelOrder(Long orderId, Long customerId, boolean isAdmin) {
        Order order = getOrderById(orderId, customerId, isAdmin);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Don hang da duoc huy truoc do");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chi co the huy don hang dang o trang thai PENDING");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID && !isAdmin) {
            throw new IllegalStateException("Don hang da thanh toan, vui long lien he cua hang de huy");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        releaseItems(saved, "huy don");
        return saved;
    }

    /** Admin cập nhật trạng thái (PENDING -> CONFIRMED -> SHIPPING -> COMPLETED, hoặc CANCELLED). */
    @Transactional
    public Order updateStatus(Long orderId, String status) {
        Order order = findOrder(orderId);
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Trang thai khong hop le. "
                    + "Gia tri hop le: PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED");
        }
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        // COD: giao hàng thành công = đã thu tiền
        if (newStatus == OrderStatus.COMPLETED && "COD".equals(order.getPaymentMethod())) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        Order saved = orderRepository.save(order);
        if (newStatus == OrderStatus.CANCELLED) {
            releaseItems(saved, "admin huy don");
        }
        return saved;
    }

    // ==================== DÙNG CHO payment-service (API nội bộ) ====================

    @Transactional(readOnly = true)
    public Order getOrderForPayment(Long orderId) {
        return findOrder(orderId);
    }

    /**
     * payment-service báo thanh toán thành công -> đánh dấu PAID
     * và tự xác nhận đơn (PENDING -> CONFIRMED).
     */
    @Transactional
    public Order markPaid(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Don hang da huy, khong the thanh toan");
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        return orderRepository.save(order);
    }

    // ==================== HÀM PHỤ ====================

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay don hang id = " + orderId));
    }

    /** Gộp các dòng trùng productId (vd: gửi 2 dòng cùng sản phẩm 1 -> 1 dòng cộng số lượng). */
    private Map<Long, OrderItemRequestDTO> mergeItems(List<OrderItemRequestDTO> items) {
        Map<Long, OrderItemRequestDTO> merged = new LinkedHashMap<>();
        for (OrderItemRequestDTO item : items) {
            merged.merge(item.getProductId(), item, (a, b) -> {
                a.setQuantity(a.getQuantity() + b.getQuantity());
                return a;
            });
        }
        return merged;
    }

    private void releaseItems(Order order, String reason) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        releaseAll(quantities, reason + " #" + order.getId());
    }

    private void releaseAll(Map<Long, Integer> quantities, String reason) {
        quantities.forEach((productId, quantity) -> {
            if (!productClient.releaseStock(productId, quantity)) {
                // Giới hạn đã biết: không có Saga/Outbox, cần xử lý tay nếu product-service đang tắt
                log.error("KHONG HOAN DUOC KHO ({}): productId={}, quantity={}", reason, productId, quantity);
            }
        });
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new IllegalStateException("Don hang dang o trang thai " + newStatus);
        }
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Don hang da huy khong the cap nhat trang thai");
        }
        if (currentStatus == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Don hang da hoan thanh khong the cap nhat trang thai");
        }
        if (currentStatus == OrderStatus.PENDING
                && (newStatus == OrderStatus.SHIPPING || newStatus == OrderStatus.COMPLETED)) {
            throw new IllegalStateException("Khong the chuyen tu PENDING sang " + newStatus + " truc tiep");
        }
        if (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.PENDING) {
            throw new IllegalStateException("Khong the quay lai PENDING");
        }
        if (currentStatus == OrderStatus.SHIPPING
                && (newStatus == OrderStatus.PENDING || newStatus == OrderStatus.CONFIRMED)) {
            throw new IllegalStateException("Don hang dang giao khong the quay lai trang thai truoc");
        }
        if (currentStatus == OrderStatus.SHIPPING && newStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Don hang dang giao khong the huy");
        }
    }
}
