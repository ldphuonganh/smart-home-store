package vn.edu.crs.auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.auth_service.dto.LoginRequestDTO;
import vn.edu.crs.auth_service.dto.LoginResponseDTO;
import vn.edu.crs.auth_service.dto.RegisterRequestDTO;
import vn.edu.crs.auth_service.entity.User;
import vn.edu.crs.auth_service.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. POST /api/auth/register - Đăng ký tài khoản mới (CUSTOMER / ADMIN)
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequestDTO dto) {
        User createdUser = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // 2. POST /api/auth/login - Đăng nhập nhận JWT Token
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    // 3. GET /api/auth/me - Lấy thông tin người dùng hiện tại từ JWT Token
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa xác thực JWT!");
        }
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(currentUser);
    }
}