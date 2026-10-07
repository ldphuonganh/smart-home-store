package vn.edu.smarthome.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dữ liệu ADMIN gửi lên khi thêm/sửa sản phẩm (giống form admin của SmartHome Store).
 * Ảnh upload riêng qua POST /products/{id}/images (multipart).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Vui lòng nhập tên sản phẩm.")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    private String name;

    @NotNull(message = "Vui lòng nhập giá sản phẩm.")
    @DecimalMin(value = "0", message = "Giá không được nhỏ hơn 0.")
    @DecimalMax(value = "999999999", message = "Giá tối đa 999.999.999đ")
    @Digits(integer = 15, fraction = 0, message = "Giá phải là số nguyên (VNĐ)")
    private BigDecimal price;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String description;

    /** Link ảnh ngoài (không bắt buộc) - thêm làm ảnh đại diện nếu sản phẩm chưa có ảnh. */
    @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
    private String imageUrl;

    @NotNull(message = "Vui lòng nhập số lượng tồn kho.")
    @Min(value = 0, message = "Tồn kho không được nhỏ hơn 0.")
    @Max(value = 999999, message = "Tồn kho tối đa 999.999")
    private Integer stock;

    @NotNull(message = "Vui lòng chọn danh mục.")
    private Long categoryId;

    @Size(max = 100, message = "Thương hiệu tối đa 100 ký tự")
    private String brand;

    @Size(max = 100, message = "Chất liệu tối đa 100 ký tự")
    private String material;

    @Size(max = 50, message = "Trọng lượng tối đa 50 ký tự")
    private String weight;

    @Size(max = 50, message = "Công suất tối đa 50 ký tự")
    private String power;

    @Size(max = 100, message = "Xuất xứ tối đa 100 ký tự")
    private String origin;

    /** Đang bán (hiển thị ở cửa hàng). null = true. */
    private Boolean active;

    public ProductRequest(String name, BigDecimal price, String description, String imageUrl,
                          Integer stock, Long categoryId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.categoryId = categoryId;
    }
}
