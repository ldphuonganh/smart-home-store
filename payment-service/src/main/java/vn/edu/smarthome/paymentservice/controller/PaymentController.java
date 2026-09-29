package vn.edu.smarthome.paymentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.paymentservice.dto.CreatePaymentRequest;
import vn.edu.smarthome.paymentservice.dto.PaymentResponse;
import vn.edu.smarthome.paymentservice.security.AuthUser;
import vn.edu.smarthome.paymentservice.service.PaymentService;

import java.util.List;

/**
 * Client gọi qua Gateway: /api/payments/** -> /payments/**
 * userId luôn lấy từ JWT; số tiền lấy từ order-service.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** Tạo (hoặc lấy lại) thanh toán cho đơn. Body: { "orderId": 1 } */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request,
                                  @AuthenticationPrincipal AuthUser user) {
        return paymentService.create(request.getOrderId(), user);
    }

    /** Giả lập ngân hàng xác nhận đã nhận chuyển khoản. */
    @PostMapping("/{id}/confirm")
    public PaymentResponse confirm(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return paymentService.confirmBankTransfer(id, user);
    }

    @PostMapping("/{id}/cancel")
    public PaymentResponse cancel(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return paymentService.cancel(id, user);
    }

    @GetMapping("/my")
    public List<PaymentResponse> getMine(@AuthenticationPrincipal AuthUser user) {
        return paymentService.getMine(user);
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return paymentService.getById(id, user);
    }

    /** ADMIN xem tất cả thanh toán. */
    @GetMapping
    public List<PaymentResponse> getAll() {
        return paymentService.getAll();
    }
}
