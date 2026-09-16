package vn.edu.crs.orderservice.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.orderservice.client.ProductClient;
import vn.edu.crs.orderservice.dto.OrderItemRequestDTO;
import vn.edu.crs.orderservice.dto.OrderRequestDTO;
import vn.edu.crs.orderservice.entity.Order;
import vn.edu.crs.orderservice.entity.OrderItem;
import vn.edu.crs.orderservice.entity.OrderStatus;
import vn.edu.crs.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductClient productClient;

    /**
     * Tạo đơn hàng / Checkout
     */
    @Transactional
    public Order createOrder(
            Long customerId,
            OrderRequestDTO dto
    ) {

        if (customerId == null) {
            throw new IllegalArgumentException(
                    "Khong xac dinh duoc customer"
            );
        }

        Order order =
                new Order();

        order.setCustomerId(
                customerId
        );

        order.setShippingAddress(
                dto.getShippingAddress().trim()
        );

        order.setPhone(
                dto.getPhone().trim()
        );

        order.setPaymentMethod(
                dto.getPaymentMethod().trim().toUpperCase()
        );

        order.setStatus(
                OrderStatus.PENDING
        );

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        for (
                OrderItemRequestDTO itemDTO :
                dto.getItems()
        ) {

            if (
                    itemDTO.getQuantity() == null
                            || itemDTO.getQuantity() <= 0
            ) {

                throw new IllegalArgumentException(
                        "So luong san pham phai lon hon 0"
                );
            }

            ProductClient.ProductInfo product =
                    productClient.getProduct(
                            itemDTO.getProductId()
                    );

            String productName;
            BigDecimal price;

            /*
             * Khi Product Service đã tích hợp:
             * lấy tên + giá từ Product Service.
             */
            if (product != null) {

                productName =
                        product.getName();

                price =
                        product.getPrice();

                if (product.getStock() != null
                        && product.getStock()
                        < itemDTO.getQuantity()) {

                    throw new IllegalStateException(
                            "San pham "
                                    + itemDTO.getProductId()
                                    + " khong du so luong ton kho"
                    );
                }

            } else {

                /*
                 * Chế độ test độc lập.
                 */
                productName =
                        itemDTO.getProductName();

                price =
                        itemDTO.getPrice();

                if (
                        productName == null
                                || productName.isBlank()
                ) {

                    productName =
                            "Product #" +
                                    itemDTO.getProductId();
                }

                if (
                        price == null
                                || price.compareTo(
                                BigDecimal.ZERO
                        ) < 0
                ) {

                    throw new IllegalArgumentException(
                            "price cua san pham khong hop le"
                    );
                }
            }

            BigDecimal subtotal =
                    price.multiply(
                            BigDecimal.valueOf(
                                    itemDTO.getQuantity()
                            )
                    );

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setProductId(
                    itemDTO.getProductId()
            );

            orderItem.setProductName(
                    productName
            );

            orderItem.setPrice(
                    price
            );

            orderItem.setQuantity(
                    itemDTO.getQuantity()
            );

            orderItem.setSubtotal(
                    subtotal
            );

            order.addItem(
                    orderItem
            );

            totalAmount =
                    totalAmount.add(
                            subtotal
                    );
        }

        order.setTotalAmount(
                totalAmount
        );

        return orderRepository.save(
                order
        );
    }

    /**
     * Customer xem các đơn của chính mình
     */
    @Transactional(readOnly = true)
    public List<Order> getMyOrders(
            Long customerId
    ) {

        return orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customerId
                );
    }

    /**
     * Admin xem toàn bộ đơn hàng
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {

        return orderRepository
                .findAllByOrderByCreatedAtDesc();
    }

    /**
     * Xem chi tiết đơn hàng
     */
    @Transactional(readOnly = true)
    public Order getOrderById(
            Long orderId,
            Long customerId,
            boolean isAdmin
    ) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "Khong tim thay don hang id = "
                                                        + orderId
                                        )
                        );

        if (
                !isAdmin
                        && !order.getCustomerId()
                        .equals(customerId)
        ) {

            throw new NoSuchElementException(
                    "Khong tim thay don hang id = "
                            + orderId
            );
        }

        return order;
    }

    /**
     * Customer hủy đơn hàng
     */
    @Transactional
    public Order cancelOrder(
            Long orderId,
            Long customerId,
            boolean isAdmin
    ) {

        Order order =
                getOrderById(
                        orderId,
                        customerId,
                        isAdmin
                );

        if (
                order.getStatus()
                        == OrderStatus.CANCELLED
        ) {

            throw new IllegalStateException(
                    "Don hang da duoc huy truoc do"
            );
        }

        if (
                order.getStatus()
                        != OrderStatus.PENDING
        ) {

            throw new IllegalStateException(
                    "Chi co the huy don hang dang o trang thai PENDING"
            );
        }

        order.setStatus(
                OrderStatus.CANCELLED
        );

        return orderRepository.save(
                order
        );
    }

    /**
     * Admin cập nhật trạng thái đơn hàng
     */
    @Transactional
    public Order updateStatus(
            Long orderId,
            String status
    ) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "Khong tim thay don hang id = "
                                                        + orderId
                                        )
                        );

        OrderStatus newStatus;

        try {

            newStatus =
                    OrderStatus.valueOf(
                            status.trim().toUpperCase()
                    );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Trang thai khong hop le. "
                            + "Gia tri hop le: "
                            + "PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED"
            );
        }

        validateStatusTransition(
                order.getStatus(),
                newStatus
        );

        order.setStatus(
                newStatus
        );

        return orderRepository.save(
                order
        );
    }

    /**
     * Kiểm tra chuyển trạng thái hợp lệ
     */
    private void validateStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (
                currentStatus
                        == OrderStatus.CANCELLED
        ) {

            throw new IllegalStateException(
                    "Don hang da huy khong the cap nhat trang thai"
            );
        }

        if (
                currentStatus
                        == OrderStatus.COMPLETED
        ) {

            throw new IllegalStateException(
                    "Don hang da hoan thanh khong the cap nhat trang thai"
            );
        }

        if (
                currentStatus
                        == OrderStatus.PENDING
                        && (
                        newStatus == OrderStatus.SHIPPING
                                || newStatus == OrderStatus.COMPLETED
                )
        ) {

            throw new IllegalStateException(
                    "Khong the chuyen tu PENDING sang "
                            + newStatus
                            + " truc tiep"
            );
        }

        if (
                currentStatus
                        == OrderStatus.CONFIRMED
                        && newStatus
                        == OrderStatus.PENDING
        ) {

            throw new IllegalStateException(
                    "Khong the quay lai PENDING"
            );
        }

        if (
                currentStatus
                        == OrderStatus.SHIPPING
                        && (
                        newStatus == OrderStatus.PENDING
                                || newStatus == OrderStatus.CONFIRMED
                )
        ) {

            throw new IllegalStateException(
                    "Don hang dang giao khong the quay lai trang thai truoc"
            );
        }
    }
}