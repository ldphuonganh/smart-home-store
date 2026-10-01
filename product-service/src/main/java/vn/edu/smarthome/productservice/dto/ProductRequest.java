package vn.edu.smarthome.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dữ liệu client gửi lên khi thêm/sửa sản phẩm (ADMIN).
 * Không có id, createdAt... vì các field này do server quản lý.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0", message = "Giá phải lớn hơn hoặc bằng 0")
    @Digits(integer = 15, fraction = 0, message = "Giá phải là số nguyên (VNĐ)")
    private BigDecimal price;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String description;

    @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
    private String imageUrl;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stock;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
