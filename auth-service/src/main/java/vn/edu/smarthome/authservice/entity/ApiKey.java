package vn.edu.smarthome.authservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API Key cấp cho Partner (bảng api_keys).
 * ownerName = tên Partner; scopes ví dụ "products:read".
 */
@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
public class ApiKey {

    public static final String ACTIVE = "ACTIVE";
    public static final String REVOKED = "REVOKED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_value", nullable = false, unique = true, length = 100)
    private String keyValue;

    @Column(name = "owner_name", nullable = false, length = 255)
    private String ownerName;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(nullable = false, length = 500)
    private String scopes;

    @Column(nullable = false, length = 20)
    private String status = ACTIVE;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ACTIVE;
    }
}
