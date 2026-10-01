package vn.edu.smarthome.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    /** Chỉ có với BANK_TRANSFER đang PENDING: thông tin để khách chuyển khoản / quét QR. */
    private BankTransferInfo bankTransfer;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankTransferInfo {
        private String bankBin;
        private String bankName;
        private String accountNo;
        private String accountName;
        private String transferContent;
    }
}
