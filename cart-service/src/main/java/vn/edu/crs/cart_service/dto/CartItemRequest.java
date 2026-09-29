package vn.edu.crs.cart_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** POST /cart/items - body JSON { "productId": 1, "quantity": 2 } (Tài liệu thống nhất - mục 28). */
@Data
public class CartItemRequest {

    @NotNull(message = "productId khong duoc de trong")
    private Long productId;

    @NotNull(message = "quantity khong duoc de trong")
    @Min(value = 1, message = "So luong phai lon hon 0")
    @Max(value = 99, message = "So luong toi da 99")
    private Integer quantity;
}
