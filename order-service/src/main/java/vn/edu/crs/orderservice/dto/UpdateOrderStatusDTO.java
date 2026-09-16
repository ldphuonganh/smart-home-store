package vn.edu.crs.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {

    @NotBlank(message = "status khong duoc de trong")
    private String status;
}