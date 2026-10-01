package vn.edu.smarthome.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyCreateRequest {

    @NotBlank(message = "Tên Partner không được để trống")
    @Size(max = 255, message = "Tên Partner tối đa 255 ký tự")
    private String ownerName;

    @Email(message = "Email liên hệ không hợp lệ")
    private String contactEmail;

    /** Nhiều scope cách nhau bởi dấu phẩy, ví dụ "products:read". */
    @NotBlank(message = "Scope không được để trống")
    @Pattern(regexp = "^[a-z]+:[a-z-]+(\\s*,\\s*[a-z]+:[a-z-]+)*$",
            message = "Scope phải có dạng resource:action, ví dụ products:read")
    private String scopes;

    /** Số ngày hiệu lực; bỏ trống = không hết hạn. */
    @Min(value = 1, message = "Số ngày hiệu lực phải lớn hơn 0")
    @Max(value = 3650, message = "Số ngày hiệu lực tối đa 3650")
    private Integer validDays;
}
