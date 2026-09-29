package vn.edu.smarthome.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    private Long id;
    private String keyValue;
    private String ownerName;
    private String contactEmail;
    private String scopes;
    /** ACTIVE, REVOKED hoặc EXPIRED (tính theo expiresAt). */
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
