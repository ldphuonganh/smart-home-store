package vn.edu.crs.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequestDTO {

    @NotNull(message = "productId khong duoc de trong")
    private Long productId;

    @NotNull(message = "quantity khong duoc de trong")
    @Min(value = 1, message = "quantity phai lon hon hoac bang 1")
    private Integer quantity;

    /*
     * Hai trường này dùng cho giai đoạn test độc lập.
     *
     * Khi Product Service hoàn thiện và
     * product-service.validation-enabled=true,
     * Order Service sẽ lấy tên và giá từ Product Service.
     */
    private String productName;

    private java.math.BigDecimal price;
}