package vn.edu.crs.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Một dòng trong giỏ. Tên, giá, ảnh, tồn kho lấy TRỰC TIẾP từ product-service mỗi lần xem
 * (giỏ hàng không lưu giá để luôn đúng giá hiện tại).
 * available = false nếu sản phẩm đã bị xoá / không lấy được thông tin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    private Integer quantity;
    private BigDecimal subtotal;
    private boolean available;
}
