package vn.edu.crs.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiKeyCreateRequestDTO {
    @NotBlank(message = "Ten doi tac khong duoc de trong")
    private String ownerName;

    @NotBlank(message = "Danh sach scope khong duoc de trong")
    private String scopes; // Ví dụ: "courses:read"

    private Integer validDays; // Số ngày hiệu lực
}