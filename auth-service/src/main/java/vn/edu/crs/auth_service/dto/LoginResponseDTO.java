package vn.edu.crs.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private Long userId;
    private String token;
    private String username;
    private String role;
}