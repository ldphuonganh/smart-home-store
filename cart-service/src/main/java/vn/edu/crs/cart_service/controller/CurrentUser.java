package vn.edu.crs.cart_service.controller;

import org.springframework.security.core.Authentication;

/** Lấy userId do JwtAuthFilter đặt vào credentials. */
final class CurrentUser {

    private CurrentUser() {
    }

    static Long id(Authentication authentication) {
        if (authentication != null && authentication.getCredentials() instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalStateException("Khong lay duoc userId tu JWT");
    }
}
