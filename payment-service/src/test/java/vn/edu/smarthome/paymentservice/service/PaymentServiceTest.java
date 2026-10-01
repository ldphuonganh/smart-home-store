package vn.edu.smarthome.paymentservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Unit test PaymentService: giả lập order-service + DB bằng Mockito. */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderClient orderClient;

    private PaymentService paymentService;

    private final AuthUser customer = new AuthUser(7L, "c@gmail.com", "CUSTOMER");

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, orderClient,
                "970436", "Vietcombank", "0123456789", "SMARTHOME STORE");
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getId() == null) p.setId(100L);
            return p;
        });
    }

    @Test
    void createUsesAmountFromOrderServiceAndReturnsBankInfo() {
        when(orderClient.getOrder(1L)).thenReturn(order(7L, "BANK_TRANSFER", "PENDING", "UNPAID"));
        when(paymentRepository.findFirstByOrderIdAndStatusIn(anyLong(), anyList())).thenReturn(Optional.empty());

        PaymentResponse res = paymentService.create(1L, customer);

        assertThat(res.getAmount()).isEqualByComparingTo("1240000");
        assertThat(res.getStatus()).isEqualTo("PENDING");
        assertThat(res.getBankTransfer().getTransferContent()).isEqualTo("SH1");
    }

    @Test
    void cannotPayOtherCustomersOrder() {
        when(orderClient.getOrder(1L)).thenReturn(order(99L, "BANK_TRANSFER", "PENDING", "UNPAID"));
        assertThatThrownBy(() -> paymentService.create(1L, customer)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void orderWithoutOwnerIsTreatedAsNotFound() {
        // order-service trả thiếu customerId -> 404, không được nổ NullPointerException (500)
        when(orderClient.getOrder(1L)).thenReturn(order(null, "BANK_TRANSFER", "PENDING", "UNPAID"));
        assertThatThrownBy(() -> paymentService.create(1L, customer)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cannotPayCancelledOrAlreadyPaidOrder() {
        when(orderClient.getOrder(1L)).thenReturn(order(7L, "BANK_TRANSFER", "CANCELLED", "UNPAID"));
        assertThatThrownBy(() -> paymentService.create(1L, customer)).isInstanceOf(ConflictException.class);

        when(orderClient.getOrder(2L)).thenReturn(order(7L, "BANK_TRANSFER", "CONFIRMED", "PAID"));
        assertThatThrownBy(() -> paymentService.create(2L, customer)).isInstanceOf(ConflictException.class);
    }

    @Test
    void confirmBankTransferMarksOrderPaidThenPayment() {
        Payment p = pending(PaymentMethod.BANK_TRANSFER);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(p));

        PaymentResponse res = paymentService.confirmBankTransfer(100L, customer);

        verify(orderClient).markPaid(1L);
        assertThat(res.getStatus()).isEqualTo("PAID");
        assertThat(res.getTransactionId()).startsWith("TXN-");
        assertThat(res.getBankTransfer()).isNull();
    }

    @Test
    void paymentStaysPendingWhenOrderServiceFails() {
        Payment p = pending(PaymentMethod.BANK_TRANSFER);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(p));
        doThrow(new ConflictException("down")).when(orderClient).markPaid(1L);

        assertThatThrownBy(() -> paymentService.confirmBankTransfer(100L, customer))
                .isInstanceOf(ConflictException.class);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void codCannotBeConfirmedOnline() {
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(pending(PaymentMethod.COD)));
        assertThatThrownBy(() -> paymentService.confirmBankTransfer(100L, customer))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(orderClient);
    }

    private static OrderClient.OrderInfo order(Long customerId, String method, String status, String paymentStatus) {
        return new OrderClient.OrderInfo(1L, customerId, new BigDecimal("1240000"), method, status, paymentStatus);
    }

    private static Payment pending(PaymentMethod method) {
        Payment p = new Payment();
        p.setId(100L);
        p.setOrderId(1L);
        p.setUserId(7L);
        p.setAmount(new BigDecimal("1240000"));
        p.setPaymentMethod(method);
        p.setStatus(PaymentStatus.PENDING);
        p.setTransferContent("SH1");
        return p;
    }
}
