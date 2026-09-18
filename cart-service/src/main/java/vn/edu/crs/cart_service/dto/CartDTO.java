package vn.edu.crs.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long id;

    private Long userId;

    private List<CartItemDTO> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}