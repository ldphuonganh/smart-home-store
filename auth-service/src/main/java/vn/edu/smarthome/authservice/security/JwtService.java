package vn.edu.smarthome.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.edu.smarthome.authservice.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Sinh và kiểm tra JWT.
 * Payload thống nhất cả nhóm: sub = email, userId, email, role (CUSTOMER / ADMIN).
 * Các service khác verify bằng cùng jwt.secret.
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Ném exception nếu token sai chữ ký / hết hạn / sai định dạng. */
    public AuthUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Number userId = claims.get("userId", Number.class);
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        return new AuthUser(userId != null ? userId.longValue() : null,
                email != null ? email : claims.getSubject(),
                role);
    }
}
