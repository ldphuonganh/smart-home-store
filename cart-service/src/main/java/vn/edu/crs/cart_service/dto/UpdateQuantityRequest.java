package vn.edu.crs.cart_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** PUT /cart/items/{id} - body JSON { "quantity": 3 }; {id} là id của DÒNG trong giỏ (cartItemId). */
@Data
public class UpdateQuantityRequest {

    @NotNull(message = "quantity khong duoc de trong")
    @Min(value = 1, message = "So luong phai lon hon 0")
    @Max(value = 99, message = "So luong toi da 99")
    private Integer quantity;
}
