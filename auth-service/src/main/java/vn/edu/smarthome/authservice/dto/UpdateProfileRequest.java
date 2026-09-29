package vn.edu.smarthome.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PUT /auth/me - người dùng tự sửa thông tin (không sửa được email/role). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
    private String fullName;

    @Pattern(regexp = "^(0\\d{9,10})?$", message = "Số điện thoại không hợp lệ")
    private String phone;
}
