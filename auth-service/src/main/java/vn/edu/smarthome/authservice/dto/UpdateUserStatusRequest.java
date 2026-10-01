package vn.edu.smarthome.authservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PUT /admin/users/{id}/status - Admin khoá / mở khoá tài khoản. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean enabled;
}
