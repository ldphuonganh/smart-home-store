package vn.edu.smarthome.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.authservice.dto.*;
import vn.edu.smarthome.authservice.security.AuthUser;
import vn.edu.smarthome.authservice.service.AuthService;

/**
 * Client gọi qua Gateway: /api/auth/**  ->  auth-service /auth/**
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Lấy thông tin tài khoản hiện tại từ JWT (không nhận id từ client - chống IDOR). */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser currentUser) {
        return authService.getById(currentUser.id());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal AuthUser currentUser,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(currentUser.id(), request);
    }
}
