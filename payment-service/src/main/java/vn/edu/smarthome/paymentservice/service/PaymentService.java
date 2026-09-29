package vn.edu.smarthome.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.paymentservice.client.OrderClient;
import vn.edu.smarthome.paymentservice.dto.PaymentResponse;
import vn.edu.smarthome.paymentservice.entity.Payment;
import vn.edu.smarthome.paymentservice.entity.PaymentMethod;
import vn.edu.smarthome.paymentservice.entity.PaymentStatus;
import vn.edu.smarthome.paymentservice.exception.BadRequestException;
import vn.edu.smarthome.paymentservice.exception.ConflictException;
import vn.edu.smarthome.paymentservice.exception.ResourceNotFoundException;
import vn.edu.smarthome.paymentservice.repository.PaymentRepository;
import vn.edu.smarthome.paymentservice.security.AuthUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final PaymentResponse.BankTransferInfo bankTemplate;

    public PaymentService(PaymentRepository paymentRepository, OrderClient orderClient,
                          @Value("${payment.bank.bin}") String bankBin,
                          @Value("${payment.bank.name}") String bankName,
                          @Value("${payment.bank.account-no}") String accountNo,
                          @Value("${payment.bank.account-name}") String accountName) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.bankTemplate = new PaymentResponse.BankTransferInfo(bankBin, bankName, accountNo, accountName, null);
    }

    /**
     * Tạo thanh toán cho đơn hàng của CHÍNH người dùng.
     * - Số tiền + phương thức lấy từ order-service.
     * - Đơn đã có thanh toán đang chờ -> trả lại thanh toán đó (bấm nhiều lần không tạo trùng).
     */
    @Transactional
    public PaymentResponse create(Long orderId, AuthUser user) {
        OrderClient.OrderInfo order = orderClient.getOrder(orderId);
        if (!Objects.equals(order.customerId(), user.id())) {
            // Không tiết lộ đơn của người khác có tồn tại hay không (chống IDOR)
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng id = " + orderId);
        }
        if ("CANCELLED".equals(order.status())) {
            throw new ConflictException("Đơn hàng đã bị huỷ, không thể thanh toán");
        }
        if ("PAID".equals(order.paymentStatus())) {
            throw new ConflictException("Đơn hàng đã được thanh toán");
        }

        if (order.totalAmount() == null || order.totalAmount().signum() <= 0) {
            throw new ConflictException("Đơn hàng không có số tiền hợp lệ để thanh toán");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(order.paymentMethod());
        } catch (Exception e) {
            throw new BadRequestException("Phương thức thanh toán của đơn không được hỗ trợ: " + order.paymentMethod());
        }

        Payment payment = paymentRepository
                .findFirstByOrderIdAndStatusIn(orderId, List.of(PaymentStatus.PENDING))
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setOrderId(orderId);
                    p.setUserId(order.customerId());
                    p.setAmount(order.totalAmount());
                    p.setPaymentMethod(method);
                    p.setStatus(PaymentStatus.PENDING);
                    p.setTransferContent("SH" + orderId);
                    return paymentRepository.save(p);
                });
        return toResponse(payment);
    }

    /**
     * GIẢ LẬP ngân hàng xác nhận đã nhận tiền (đồ án không kết nối ngân hàng thật).
     * Báo order-service trước; chỉ khi order-service cập nhật thành công mới đánh dấu PAID.
     */
    @Transactional
    public PaymentResponse confirmBankTransfer(Long paymentId, AuthUser user) {
        Payment payment = findOwned(paymentId, user);
        if (payment.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new BadRequestException("Thanh toán COD được xác nhận khi giao hàng, không xác nhận online");
        }
        requirePending(payment);
        orderClient.markPaid(payment.getOrderId());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse cancel(Long paymentId, AuthUser user) {
        Payment payment = findOwned(paymentId, user);
        requirePending(payment);
        payment.setStatus(PaymentStatus.CANCELLED);
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Long paymentId, AuthUser user) {
        return toResponse(findOwned(paymentId, user));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMine(AuthUser user) {
        return paymentRepository.findByUserIdOrderByIdDesc(user.id()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    // ==================== HÀM PHỤ ====================

    /** Admin xem được mọi thanh toán; Customer chỉ xem thanh toán của mình. */
    private Payment findOwned(Long paymentId, AuthUser user) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thanh toán id = " + paymentId));
        if (!user.isAdmin() && !payment.getUserId().equals(user.id())) {
            throw new ResourceNotFoundException("Không tìm thấy thanh toán id = " + paymentId);
        }
        return payment;
    }

    private void requirePending(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Thanh toán đang ở trạng thái " + payment.getStatus() + ", không thể thực hiện");
        }
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse.BankTransferInfo bank = null;
        if (p.getPaymentMethod() == PaymentMethod.BANK_TRANSFER && p.getStatus() == PaymentStatus.PENDING) {
            bank = new PaymentResponse.BankTransferInfo(bankTemplate.getBankBin(), bankTemplate.getBankName(),
                    bankTemplate.getAccountNo(), bankTemplate.getAccountName(), p.getTransferContent());
        }
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .userId(p.getUserId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod().name())
                .status(p.getStatus().name())
                .transactionId(p.getTransactionId())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .bankTransfer(bank)
                .build();
    }
}
