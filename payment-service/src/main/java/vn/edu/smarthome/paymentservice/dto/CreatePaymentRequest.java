package vn.edu.smarthome.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POST /payments - chỉ cần orderId.
 * Số tiền, phương thức, chủ đơn đều lấy từ order-service (client không sửa được số tiền).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "orderId không được để trống")
    private Long orderId;
}
