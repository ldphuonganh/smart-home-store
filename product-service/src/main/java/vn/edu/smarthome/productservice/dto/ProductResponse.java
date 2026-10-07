package vn.edu.smarthome.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu sản phẩm trả về cho client.
 * Trả kèm categoryName/categorySlug, danh sách ảnh và điểm đánh giá trung bình
 * để Frontend không phải gọi thêm API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String brand;
    private String material;
    private String weight;
    private String power;
    private String origin;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private List<ImageResponse> images;
    private Integer stock;
    /** "Còn hàng" (> 10), "Sắp hết hàng" (1-10), "Hết hàng" (0). */
    private String stockStatus;
    private boolean active;
    private Integer soldCount;
    private Double averageRating;
    private Long reviewCount;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageResponse {
        private Long id;
        private String imageUrl;
        private boolean primary;
    }
}
