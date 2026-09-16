// ApiKeyCreateRequestDTO.java
package vn.edu.crs.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiKeyCreateRequestDTO {

    @NotBlank(message = "Tên đối tác không được để trống")
    private String ownerName;

    @NotBlank(message = "Scope không được để trống")
    private String scopes; // Ví dụ: "courses:read,courses:write"

    private Integer validDays; // Số ngày có hiệu lực
}