package vn.edu.smarthome.productservice.security;

/**
 * Thông tin người dùng lấy từ JWT (auth-service phát hành).
 * Dùng làm principal trong SecurityContext: @AuthenticationPrincipal AuthUser user.
 */
public record AuthUser(Long userId, String email, String name, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return email;
    }
}
