package vn.edu.smarthome.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Đúng format Tài liệu thống nhất - mục 22: { accessToken, tokenType, user }. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private UserResponse user;
}
