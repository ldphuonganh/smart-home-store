package vn.edu.crs.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.orderservice.entity.Order;
import vn.edu.crs.orderservice.service.OrderService;

import java.math.BigDecimal;

/**
 * API NỘI BỘ cho payment-service (gọi thẳng cổng 8083, Gateway KHÔNG định tuyến /internal/**).
 * payment-service lấy tổng tiền từ đây, KHÔNG tin số tiền client gửi lên.
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public OrderPaymentInfo getForPayment(@PathVariable Long id) {
        return OrderPaymentInfo.from(orderService.getOrderForPayment(id));
    }

    /** payment-service báo đã thanh toán -> đơn chuyển PAID + CONFIRMED. */
    @PutMapping("/{id}/paid")
    public OrderPaymentInfo markPaid(@PathVariable Long id) {
        return OrderPaymentInfo.from(orderService.markPaid(id));
    }

    public record OrderPaymentInfo(Long id, Long customerId, BigDecimal totalAmount,
                                   String paymentMethod, String status, String paymentStatus) {
        static OrderPaymentInfo from(Order order) {
            return new OrderPaymentInfo(order.getId(), order.getCustomerId(), order.getTotalAmount(),
                    order.getPaymentMethod(), order.getStatus().name(), order.getPaymentStatus().name());
        }
    }
}
