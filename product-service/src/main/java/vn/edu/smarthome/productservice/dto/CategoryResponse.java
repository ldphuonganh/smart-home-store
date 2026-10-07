package vn.edu.smarthome.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String image;
    /** Số sản phẩm đang thuộc danh mục (Admin dùng để biết có xoá được không). */
    private Long productCount;
}
