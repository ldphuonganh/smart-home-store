package vn.edu.smarthome.paymentservice.security;

/** Người dùng đang đăng nhập, lấy từ JWT. */
public record AuthUser(Long id, String email, String role) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
