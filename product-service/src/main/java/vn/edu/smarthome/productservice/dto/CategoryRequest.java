package vn.edu.smarthome.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Vui lòng nhập tên danh mục.")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự.")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Tên danh mục chỉ được chứa chữ cái và khoảng trắng.")
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @Size(max = 500, message = "Đường dẫn ảnh tối đa 500 ký tự")
    private String image;

    public CategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
