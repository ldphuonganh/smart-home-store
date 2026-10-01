package vn.edu.smarthome.authservice.security;

/** Người dùng đang đăng nhập, lấy từ JWT (đặt làm principal trong SecurityContext). */
public record AuthUser(Long id, String email, String role) {
}
