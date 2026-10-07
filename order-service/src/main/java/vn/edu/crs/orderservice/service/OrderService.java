package vn.edu.crs.orderservice.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.orderservice.client.ProductClient;
import vn.edu.crs.orderservice.config.ShopProperties;
import vn.edu.crs.orderservice.dto.*;
import vn.edu.crs.orderservice.entity.*;
import vn.edu.crs.orderservice.exception.ApiException;
import vn.edu.crs.orderservice.repository.OrderItemRepository;
import vn.edu.crs.orderservice.repository.OrderRepository;
import vn.edu.crs.orderservice.repository.PromotionRepository;
import vn.edu.crs.orderservice.security.AuthUser;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Nghiệp vụ đơn hàng - chuyển từ SmartHome Store (Laravel) sang microservice:
 * tính tiền (tạm tính, mã giảm giá, phí ship), đặt hàng, huỷ, sửa đơn, luồng trạng thái,
 * ghi nhận thanh toán, tra cứu, thống kê.
 *
 * Khác bản Laravel: tồn kho nằm ở product-service (DB khác) nên không khoá dòng được;
 * dùng API trừ kho nguyên tử + BÙ TRỪ (hoàn kho) khi đặt hàng lỗi giữa chừng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<String> CHANNELS = Set.of("VNBANK", "INTCARD");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PromotionRepository promotionRepository;
    private final ProductClient productClient;
    private final ShippingService shippingService;
    private final ShopProperties shop;

    // =====================================================================
    // TÍNH TIỀN (trang thanh toán)
    // =====================================================================

    /** Dữ liệu tạm tính dùng chung cho quote và đặt hàng. */
    private record Priced(List<QuoteResponse.Line> lines, List<String> stockErrors,
                          Map<Long, ProductClient.ProductInfo> products, BigDecimal subtotal, int quantity) {
    }

    private Priced price(List<CheckoutItem> requested) {
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (CheckoutItem item : requested) {
            merged.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        List<QuoteResponse.Line> lines = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<Long, ProductClient.ProductInfo> products = new LinkedHashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int quantity = 0;
        for (Map.Entry<Long, Integer> e : merged.entrySet()) {
            ProductClient.ProductInfo p = productClient.getProduct(e.getKey());
            int qty = e.getValue();
            int stock = p.getStock() == null ? 0 : p.getStock();
            products.put(p.getId(), p);
            if (!p.isOnSale()) {
                errors.add("\"" + p.getName() + "\" đã ngừng kinh doanh.");
            } else if (stock < qty) {
                errors.add("\"" + p.getName() + "\" chỉ còn " + stock + " sản phẩm (bạn đang chọn " + qty + ").");
            }
            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(qty));
            lines.add(new QuoteResponse.Line(p.getId(), p.getName(), p.getSlug(), p.getImageUrl(), p.getPrice(),
                    qty, line, stock, p.isOnSale()));
            subtotal = subtotal.add(line);
            quantity += qty;
        }
        return new Priced(lines, errors, products, subtotal, quantity);
    }

    /** Tóm tắt tiền cho trang thanh toán: mọi con số do SERVER tính. */
    @Transactional(readOnly = true)
    public QuoteResponse quote(QuoteRequest request) {
        Priced priced = price(request.getItems());
        BigDecimal subtotal = priced.subtotal();

        Promotion promotion = null;
        BigDecimal discount = BigDecimal.ZERO;
        String promotionError = null;
        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            promotion = promotionRepository.findByCodeIgnoreCase(request.getPromotionCode().trim()).orElse(null);
            if (promotion == null) {
                promotionError = "Mã giảm giá không tồn tại!";
            } else if ((promotionError = promotion.validateFor(subtotal)) == null) {
                discount = promotion.calculateDiscount(subtotal);
            } else {
                promotion = null;
            }
        }
        BigDecimal afterDiscount = subtotal.subtract(discount);

        String[] choice = shippingService.resolveChoice(request.getShippingZone(), request.getShippingMethod());
        BigDecimal shippingFee;
        QuoteResponse.CarrierService carrierService = null;
        String deliveryLabel;
        ViettelPostClient.CarrierService service = request.usesCarrier()
                ? shippingService.carrierQuote(request.getShippingServiceCode(), request.getShippingProvinceId(),
                request.getShippingDistrictId(), priced.quantity(), subtotal)
                : null;
        if (service != null) {
            shippingFee = shippingService.chargeableCarrierFee(afterDiscount, service.fee());
            carrierService = new QuoteResponse.CarrierService(service.code(), service.name(), service.fee(), service.time());
            deliveryLabel = service.time() != null ? "Dự kiến giao trong " + service.time() : null;
        } else {
            shippingFee = shippingService.zoneFee(afterDiscount, choice[0], choice[1]);
            deliveryLabel = shippingService.deliveryLabel(choice[0], choice[1]);
        }
        return new QuoteResponse(priced.lines(), priced.stockErrors(), priced.quantity(), subtotal, discount,
                shippingFee, afterDiscount.add(shippingFee),
                promotion != null ? promotion.getCode() : null,
                promotion != null ? promotion.getOfferLabel() : null,
                promotionError, shippingService.qualifiesForFreeShipping(afterDiscount),
                shippingService.freeShippingThreshold(), choice[0], choice[1], deliveryLabel, carrierService);
    }

    // =====================================================================
    // ĐẶT HÀNG
    // =====================================================================

    /**
     * Đặt hàng:
     * 1. Lấy giá/tồn kho THẬT từ product-service, chặn sản phẩm ngừng bán / không đủ hàng.
     * 2. Kiểm tra lại mã giảm giá NGAY LÚC ĐẶT (khoá dòng mã) và tăng lượt dùng.
     * 3. Tính lại cước ở server (hãng vận chuyển hoặc bảng phí khu vực).
     * 4. Trừ kho từng sản phẩm; lỗi giữa chừng -> hoàn kho phần đã trừ (bù trừ).
     * 5. Lưu đơn kèm snapshot tên, ảnh, giá.
     */
    @Transactional
    public Order createOrder(AuthUser user, CheckoutRequest request) {
        PaymentMethod method = PaymentMethod.parse(request.getPaymentMethod());
        if (method == PaymentMethod.PAYPAL && !shop.isPaypalEnabled()) {
            throw ApiException.badRequest("paymentMethod", "Cửa hàng chưa hỗ trợ thanh toán PayPal.");
        }
        String channel = method == PaymentMethod.VNPAY ? blankToNull(request.getPaymentChannel()) : null;
        if (channel != null && !CHANNELS.contains(channel)) {
            throw ApiException.badRequest("paymentChannel", "Kênh thanh toán không hợp lệ");
        }
        if (!request.usesCarrier() && "express".equals(request.getShippingMethod())
                && shippingService.deliveryDays(request.getShippingZone(), "express") == null) {
            throw ApiException.badRequest("shippingMethod",
                    "Khu vực bạn chọn hiện chưa hỗ trợ giao hàng nhanh, vui lòng chọn giao hàng tiêu chuẩn.");
        }

        Priced priced = price(request.getItems());
        for (ProductClient.ProductInfo p : priced.products().values()) {
            if (!p.isOnSale()) {
                throw ApiException.conflict("Sản phẩm trong giỏ đã ngừng kinh doanh, vui lòng kiểm tra lại giỏ hàng.");
            }
        }
        if (!priced.stockErrors().isEmpty()) {
            throw ApiException.conflict(priced.stockErrors().get(0));
        }
        BigDecimal subtotal = priced.subtotal();

        // 2. Mã giảm giá: kiểm tra lại + khoá dòng + tăng lượt dùng (rollback nếu đặt hàng lỗi)
        Promotion promotion = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            Promotion found = promotionRepository.findByCodeIgnoreCase(request.getPromotionCode().trim())
                    .orElseThrow(() -> ApiException.conflict("Mã giảm giá không còn tồn tại. Vui lòng kiểm tra lại đơn hàng."));
            promotion = promotionRepository.findByIdForUpdate(found.getId()).orElseThrow();
            String error = promotion.validateFor(subtotal);
            if (error != null) {
                throw ApiException.conflict(error + " Vui lòng kiểm tra lại đơn hàng.");
            }
            discount = promotion.calculateDiscount(subtotal);
            promotion.setUsedCount(promotion.getUsedCount() + 1);
        }
        BigDecimal afterDiscount = subtotal.subtract(discount);

        // 3. Cước vận chuyển luôn tính lại ở server
        Order order = new Order();
        String[] choice = shippingService.resolveChoice(request.getShippingZone(), request.getShippingMethod());
        ViettelPostClient.CarrierService service = request.usesCarrier()
                ? shippingService.carrierQuote(request.getShippingServiceCode(), request.getShippingProvinceId(),
                request.getShippingDistrictId(), priced.quantity(), subtotal)
                : null;
        BigDecimal shippingFee;
        if (service != null) {
            shippingFee = shippingService.chargeableCarrierFee(afterDiscount, service.fee());
            applyCarrier(order, request, service);
        } else {
            shippingFee = shippingService.zoneFee(afterDiscount, choice[0], choice[1]);
        }
        order.setShippingZone(choice[0]);
        order.setShippingMethod(choice[1]);

        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(user.id());
        order.setCustomerName(user.name());
        order.setCustomerEmail(user.email());
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(afterDiscount.add(shippingFee));
        if (promotion != null) {
            order.setPromotionId(promotion.getId());
            order.setPromotionCode(promotion.getCode());
        }
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(method);
        order.setPaymentChannel(channel);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setShippingName(request.getShippingName().trim());
        order.setShippingPhone(request.getShippingPhone().trim());
        order.setShippingAddress(request.getShippingAddress().trim());
        order.setNote(blankToNull(request.getNote()));

        // 4. Trừ kho + 5. snapshot dòng đơn
        Map<Long, Integer> reserved = new LinkedHashMap<>();
        try {
            for (QuoteResponse.Line line : priced.lines()) {
                productClient.reserveStock(line.productId(), line.quantity());
                reserved.put(line.productId(), line.quantity());
                OrderItem item = new OrderItem();
                item.setProductId(line.productId());
                item.setProductName(line.productName());
                item.setProductSlug(line.productSlug());
                item.setProductImage(line.imageUrl());
                item.setPrice(line.price());
                item.setQuantity(line.quantity());
                item.setSubtotal(line.subtotal());
                order.addItem(item);
            }
            return orderRepository.saveAndFlush(order);
        } catch (RuntimeException e) {
            releaseAll(reserved, "đặt hàng thất bại");
            throw e;
        }
    }

    // =====================================================================
    // KHÁCH HÀNG: XEM / HUỶ / SỬA
    // =====================================================================

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(Long customerId, int page, int size) {
        var result = orderRepository.findByCustomerId(customerId, PageRequest.of(Math.max(page, 0),
                Math.max(1, Math.min(size, 50)), Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return PageResponse.from(result.map(this::toResponse));
    }

    /** Khách chỉ xem được đơn của mình (đơn người khác trả 404 - chống IDOR). */
    @Transactional(readOnly = true)
    public Order getForUser(Long orderId, AuthUser user) {
        Order order = find(orderId);
        if (!user.isAdmin() && !order.getCustomerId().equals(user.id())) {
            throw ApiException.notFound("Không tìm thấy đơn hàng");
        }
        order.getItems().size();
        return order;
    }

    /** Khách tự huỷ khi shop CHƯA xác nhận. */
    @Transactional
    public Order cancelByCustomer(Long orderId, AuthUser user, String reason) {
        Order order = getForUser(orderId, user);
        if (!order.isCancellableByCustomer()) {
            throw ApiException.conflict("Đơn hàng đã được shop xác nhận, vui lòng liên hệ shop để được hỗ trợ hủy.");
        }
        cancel(order, reason == null || reason.isBlank() ? "Khách hàng hủy" : reason.trim());
        return order;
    }

    /**
     * Khách sửa đơn (chỉ khi PENDING): người nhận, SĐT, địa chỉ, ghi chú;
     * nếu chưa trả tiền: đổi phương thức thanh toán, đổi tuyến giao (tính lại cước + tổng tiền).
     */
    @Transactional
    public Order updateByCustomer(Long orderId, AuthUser user, UpdateOrderRequest request) {
        Order order = getForUser(orderId, user);
        if (!order.isEditableByCustomer()) {
            throw ApiException.conflict("Đơn hàng đã được shop xác nhận nên không sửa được nữa. Vui lòng liên hệ shop.");
        }
        order.setShippingName(request.getShippingName().trim());
        order.setShippingPhone(request.getShippingPhone().trim());
        order.setShippingAddress(request.getShippingAddress().trim());
        order.setNote(blankToNull(request.getNote()));

        // Đã trả tiền thì KHÔNG đổi cách thanh toán, cũng không đổi tuyến (đổi cước = lệch số tiền đã thu)
        if (!order.isPaid() && order.getPaymentStatus() != PaymentStatus.PENDING_VERIFICATION) {
            if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
                PaymentMethod newMethod = PaymentMethod.parse(request.getPaymentMethod());
                if (newMethod == PaymentMethod.PAYPAL && !shop.isPaypalEnabled()) {
                    throw ApiException.badRequest("paymentMethod", "Cửa hàng chưa hỗ trợ thanh toán PayPal.");
                }
                String newChannel = newMethod == PaymentMethod.VNPAY ? blankToNull(request.getPaymentChannel()) : null;
                if (newMethod != order.getPaymentMethod() || !Objects.equals(newChannel, order.getPaymentChannel())) {
                    order.setPaymentMethod(newMethod);
                    order.setPaymentChannel(newChannel);
                    // Lần thanh toán hỏng trước đó không còn ý nghĩa với phương thức mới
                    order.setPaymentStatus(PaymentStatus.UNPAID);
                }
            }
            recalculateShipping(order, request);
        }
        return orderRepository.save(order);
    }

    private void recalculateShipping(Order order, UpdateOrderRequest request) {
        BigDecimal afterDiscount = order.getSubtotal().subtract(order.getDiscountAmount());
        if (request.usesCarrier()) {
            ViettelPostClient.CarrierService service = shippingService.carrierQuote(request.getShippingServiceCode(),
                    request.getShippingProvinceId(), request.getShippingDistrictId(), order.getTotalQuantity(),
                    order.getSubtotal());
            if (service == null) {
                return; // hãng không phản hồi -> giữ nguyên cước cũ
            }
            applyCarrier(order, request, service);
            order.setShippingFee(shippingService.chargeableCarrierFee(afterDiscount, service.fee()));
        } else if (request.getShippingZone() != null && !request.getShippingZone().isBlank()) {
            String[] choice = shippingService.resolveChoice(request.getShippingZone(), request.getShippingMethod());
            order.setShippingZone(choice[0]);
            order.setShippingMethod(choice[1]);
            clearCarrier(order);
            order.setShippingFee(shippingService.zoneFee(afterDiscount, choice[0], choice[1]));
        } else {
            return;
        }
        order.setTotalAmount(afterDiscount.add(order.getShippingFee()));
    }

    /** Lần trước khách trả bằng gì thì trang thanh toán chọn sẵn cái đó. */
    @Transactional(readOnly = true)
    public Map<String, String> lastPaymentChoice(Long customerId) {
        return orderRepository.findFirstByCustomerIdOrderByIdDesc(customerId)
                .filter(o -> o.getPaymentMethod() != null)
                .map(o -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("method", o.getPaymentMethod().name());
                    m.put("channel", o.getPaymentChannel());
                    return m;
                })
                .orElse(Map.of());
    }

    /** Tra cứu công khai bằng mã đơn + SĐT (không cần đăng nhập). */
    @Transactional(readOnly = true)
    public OrderResponse track(TrackRequest request) {
        String code = request.getOrderNumber().replace(" ", "").replace("-", "").toUpperCase();
        Order order = orderRepository.track(code, request.getPhone().trim())
                .orElseThrow(() -> ApiException.notFound(
                        "Không tìm thấy đơn hàng khớp với mã đơn và số điện thoại đã nhập."));
        return toResponse(order);
    }

    // =====================================================================
    // ADMIN
    // =====================================================================

    @Transactional(readOnly = true)
    public AdminOrderPage adminList(String search, String status, String paymentStatus, int page, int size) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                ps.add(cb.or(cb.like(cb.lower(root.get("orderNumber")), like),
                        cb.like(cb.lower(root.get("shippingName")), like),
                        cb.like(cb.lower(root.get("shippingPhone")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("customerEmail"), "")), like)));
            }
            if (status != null && !status.isBlank()) {
                ps.add(cb.equal(root.get("status"), parseStatus(status)));
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                ps.add(cb.equal(root.get("paymentStatus"), parsePaymentStatus(paymentStatus)));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
        var result = orderRepository.findAll(spec, PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", orderRepository.count());
        for (OrderStatus s : OrderStatus.values()) {
            stats.put(s.name().toLowerCase(), orderRepository.countByStatus(s));
        }
        stats.put("needVerify", orderRepository.countByPaymentStatus(PaymentStatus.PENDING_VERIFICATION));
        stats.put("needRefund", orderRepository.countByPaymentStatus(PaymentStatus.REFUND_PENDING));
        return new AdminOrderPage(PageResponse.from(result.map(this::toResponse)), stats);
    }

    /**
     * Admin chuyển trạng thái theo đúng luồng. Đơn online phải có tiền rồi mới xác nhận.
     * Huỷ -> hoàn kho, trả lượt mã giảm giá, chờ hoàn tiền nếu đã thu.
     * Hoàn thành -> COD coi như đã thu tiền; báo số lượng đã bán cho product-service.
     */
    @Transactional
    public Order updateStatus(Long orderId, UpdateStatusRequest request) {
        Order order = find(orderId);
        OrderStatus next = parseStatus(request.getStatus());
        if (next == order.getStatus()) {
            return order;
        }
        if (!order.getStatus().allowedNext().contains(next)) {
            throw ApiException.conflict("Không thể chuyển đơn từ \"" + order.getStatus().getLabel()
                    + "\" sang \"" + next.getLabel() + "\".");
        }
        if (next == OrderStatus.CONFIRMED && order.isOnlinePayment() && !order.isPaid()) {
            throw ApiException.conflict("Đơn thanh toán online chưa nhận được tiền, chưa thể xác nhận.");
        }
        if (next == OrderStatus.CANCELLED) {
            cancel(order, request.getCancelReason() == null || request.getCancelReason().isBlank()
                    ? "Shop hủy đơn" : request.getCancelReason().trim());
            return order;
        }
        order.setStatus(next);
        if (next == OrderStatus.COMPLETED) {
            if (order.getPaymentMethod() == PaymentMethod.COD) {
                markAsPaid(order, null); // giao thành công = shipper đã thu tiền
            }
            order.getItems().forEach(i -> productClient.increaseSold(i.getProductId(), i.getQuantity()));
        }
        return orderRepository.save(order);
    }

    /** Admin đối soát sao kê và xác nhận đã nhận tiền chuyển khoản QR. */
    @Transactional
    public Order confirmPayment(Long orderId) {
        Order order = find(orderId);
        if (order.getPaymentMethod() != PaymentMethod.QR || !EnumSet.of(PaymentStatus.UNPAID,
                PaymentStatus.PENDING_VERIFICATION, PaymentStatus.FAILED).contains(order.getPaymentStatus())) {
            throw ApiException.conflict("Đơn này không ở trạng thái chờ xác nhận chuyển khoản.");
        }
        markAsPaid(order, null);
        return orderRepository.save(order);
    }

    /** Admin đánh dấu đã hoàn tiền cho đơn bị huỷ. */
    @Transactional
    public Order markRefunded(Long orderId) {
        Order order = find(orderId);
        if (order.getPaymentStatus() != PaymentStatus.REFUND_PENDING) {
            throw ApiException.conflict("Đơn này không cần hoàn tiền.");
        }
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        return orderRepository.save(order);
    }

    /** Chỉ xoá được đơn ĐÃ HUỶ (đơn khác là lịch sử doanh thu). */
    @Transactional
    public String delete(Long orderId) {
        Order order = find(orderId);
        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw ApiException.conflict("Chỉ có thể xóa đơn đã hủy.");
        }
        orderRepository.delete(order);
        return order.getOrderNumber();
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        int year = LocalDate.now().getYear();
        List<Object[]> rows = orderRepository.monthlyRevenue(LocalDate.of(year, 1, 1).atStartOfDay(),
                LocalDate.of(year + 1, 1, 1).atStartOfDay());
        BigDecimal[] revenue = new BigDecimal[12];
        Arrays.fill(revenue, BigDecimal.ZERO);
        for (Object[] row : rows) {
            revenue[((Number) row[0]).intValue() - 1] = new BigDecimal(row[1].toString());
        }
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add("Tháng " + i);
        }
        Map<String, Long> statusCount = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            statusCount.put(s.name().toLowerCase(), orderRepository.countByStatus(s));
        }
        List<DashboardResponse.TopProduct> top = orderItemRepository.topProducts(PageRequest.of(0, 5)).stream()
                .map(r -> new DashboardResponse.TopProduct((Long) r[0], (String) r[1], (String) r[2],
                        ((Number) r[3]).longValue(), ((Number) r[4]).longValue()))
                .toList();
        return new DashboardResponse(orderRepository.count(), orderRepository.completedRevenue(),
                orderRepository.countByStatus(OrderStatus.PENDING), months, Arrays.asList(revenue), statusCount, top,
                orderRepository.findTop5ByOrderByCreatedAtDesc().stream().map(this::toResponse).toList());
    }

    // =====================================================================
    // THANH TOÁN (payment-service gọi qua API nội bộ)
    // =====================================================================

    @Transactional(readOnly = true)
    public Order find(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng id = " + orderId));
    }

    @Transactional(readOnly = true)
    public Order findByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber.trim().toUpperCase())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng " + orderNumber));
    }

    /**
     * Ghi nhận thanh toán thành công (VNPay, SePay, PayPal, admin xác nhận CK, COD giao xong).
     * Idempotent: gọi nhiều lần chỉ ghi nhận 1 lần. Đơn đã huỷ mà tiền về -> chờ hoàn tiền.
     * @return true nếu lần gọi này chuyển đơn sang PAID
     */
    @Transactional
    public boolean markPaid(Long orderId, String transactionId) {
        Order order = find(orderId);
        boolean paid = markAsPaid(order, transactionId);
        orderRepository.save(order);
        return paid;
    }

    /** payment-service báo thanh toán thất bại / khách báo đã chuyển khoản (chờ đối soát). */
    @Transactional
    public Order updatePaymentStatus(Long orderId, String status) {
        Order order = find(orderId);
        PaymentStatus next = parsePaymentStatus(status);
        if (next != PaymentStatus.FAILED && next != PaymentStatus.PENDING_VERIFICATION) {
            throw ApiException.badRequest("Chỉ cập nhật được FAILED hoặc PENDING_VERIFICATION");
        }
        if (order.isPaid() || order.getStatus() == OrderStatus.CANCELLED
                || order.getPaymentStatus() == PaymentStatus.REFUND_PENDING
                || order.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return order; // không ghi đè trạng thái đã chốt
        }
        order.setPaymentStatus(next);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public boolean hasPurchased(Long userId, Long productId) {
        return orderRepository.hasPurchased(userId, productId);
    }

    @Transactional(readOnly = true)
    public long activeOrderCount(Long userId) {
        return orderRepository.countByCustomerIdAndStatusNot(userId, OrderStatus.CANCELLED);
    }

    /** Huỷ các đơn online quá hạn thanh toán (chạy định kỳ). */
    @Transactional
    public int cancelExpiredUnpaidOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(shop.getPaymentTimeoutMinutes());
        List<Order> expired = orderRepository.findExpiredUnpaid(
                EnumSet.of(PaymentMethod.QR, PaymentMethod.VNPAY, PaymentMethod.PAYPAL),
                EnumSet.of(PaymentStatus.UNPAID, PaymentStatus.FAILED), deadline);
        expired.forEach(o -> cancel(o, "Quá hạn thanh toán"));
        return expired.size();
    }

    // =====================================================================
    // HÀM PHỤ
    // =====================================================================

    public OrderResponse toResponse(Order order) {
        var zone = order.getShippingZone() == null ? null : shippingService.zones().get(order.getShippingZone());
        int[] days = order.getShippingZone() == null ? null
                : shippingService.deliveryDays(order.getShippingZone(), order.getShippingMethod());
        return OrderResponse.from(order, zone == null ? null : zone.label(), days);
    }

    /** Huỷ: hoàn kho, trả lượt mã giảm giá, cập nhật trạng thái thanh toán. */
    private void cancel(Order order, String reason) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        // Lấy danh sách sản phẩm TRƯỚC khi chạy câu UPDATE trả lượt mã giảm giá:
        // trước đây câu UPDATE xoá bộ nhớ đệm JPA làm order.getItems() lỗi LazyInitialization (500)
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        order.getItems().forEach(i -> quantities.merge(i.getProductId(), i.getQuantity(), Integer::sum));
        PaymentStatus ps = order.getPaymentStatus();
        if (ps == PaymentStatus.PAID || ps == PaymentStatus.PENDING_VERIFICATION) {
            // Đã thu / khách báo đã CK -> shop phải kiểm tra & hoàn tiền
            order.setPaymentStatus(PaymentStatus.REFUND_PENDING);
        } else if (ps == PaymentStatus.FAILED) {
            order.setPaymentStatus(PaymentStatus.UNPAID);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        orderRepository.saveAndFlush(order);
        if (order.getPromotionId() != null) {
            promotionRepository.releaseUsage(order.getPromotionId());
        }
        releaseAll(quantities, "huỷ đơn " + order.getOrderNumber());
    }

    private boolean markAsPaid(Order order, String transactionId) {
        if (order.isPaid()) {
            return false;
        }
        if (order.getPaymentStatus() == PaymentStatus.REFUND_PENDING
                || order.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return false;
        }
        PaymentStatus next = order.getStatus() == OrderStatus.CANCELLED
                ? PaymentStatus.REFUND_PENDING : PaymentStatus.PAID;
        order.setPaymentStatus(next);
        if (transactionId != null) {
            order.setTransactionId(transactionId);
        }
        order.setPaidAt(LocalDateTime.now());
        return next == PaymentStatus.PAID;
    }

    private void releaseAll(Map<Long, Integer> quantities, String reason) {
        quantities.forEach((productId, quantity) -> {
            if (!productClient.releaseStock(productId, quantity)) {
                // Giới hạn đã biết: chưa có Saga/Outbox, cần xử lý tay nếu product-service đang tắt
                log.error("KHÔNG HOÀN ĐƯỢC KHO ({}): productId={}, quantity={}", reason, productId, quantity);
            }
        });
    }

    private void applyCarrier(Order order, ShippingFields f, ViettelPostClient.CarrierService service) {
        order.setShippingProvinceId(f.getShippingProvinceId());
        order.setShippingProvinceName(blankToNull(f.getShippingProvinceName()));
        order.setShippingDistrictId(f.getShippingDistrictId());
        order.setShippingDistrictName(blankToNull(f.getShippingDistrictName()));
        order.setShippingWardId(f.getShippingWardId());
        order.setShippingWardName(blankToNull(f.getShippingWardName()));
        order.setShippingCarrier("viettelpost");
        order.setShippingServiceCode(service.code());
        order.setShippingServiceName(service.name());
        order.setShippingDeliveryTime(service.time());
    }

    private void clearCarrier(Order order) {
        order.setShippingCarrier(null);
        order.setShippingServiceCode(null);
        order.setShippingServiceName(null);
        order.setShippingDeliveryTime(null);
    }

    /** VD: SH260918-K3F9QX (không có dấu "_" vì dấu này dùng ghép mã giao dịch cổng thanh toán). */
    private String generateOrderNumber() {
        String prefix = "SH" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-";
        String number;
        do {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < 6; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            number = sb.toString();
        } while (orderRepository.existsByOrderNumber(number));
        return number;
    }

    private static OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "status",
                    "Trạng thái không hợp lệ. Giá trị hợp lệ: PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED");
        }
    }

    private static PaymentStatus parsePaymentStatus(String status) {
        try {
            return PaymentStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            throw ApiException.badRequest("Trạng thái thanh toán không hợp lệ");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
