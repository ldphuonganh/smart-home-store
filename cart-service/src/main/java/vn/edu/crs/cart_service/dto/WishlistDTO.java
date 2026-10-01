package vn.edu.crs.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {

    private Long id;
    private Long userId;
    private List<WishlistItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}