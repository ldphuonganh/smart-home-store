package vn.edu.crs.cart_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistItemRequest {

    @NotNull(message = "productId khong duoc de trong")
    private Long productId;
}
