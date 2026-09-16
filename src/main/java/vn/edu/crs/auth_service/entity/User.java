package vn.edu.crs.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 150)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String role; // "ADMIN" hoặc "CUSTOMER" / "STUDENT"
}