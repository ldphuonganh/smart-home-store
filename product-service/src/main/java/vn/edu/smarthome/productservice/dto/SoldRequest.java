package vn.edu.smarthome.productservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** order-service báo số lượng đã bán khi đơn giao thành công. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoldRequest {
    @NotNull(message = "quantity không được để trống")
    @Min(value = 1, message = "quantity phải lớn hơn 0")
    private Integer quantity;
}
