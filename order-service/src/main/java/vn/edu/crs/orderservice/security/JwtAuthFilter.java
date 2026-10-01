package vn.edu.crs.orderservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        /*
         * Kiểm tra Authorization header.
         * JWT hợp lệ phải có dạng:
         *
         * Authorization: Bearer <token>
         */
        if (
                authHeader != null
                        && authHeader.startsWith("Bearer ")
        ) {

            String token = authHeader.substring(7);

            try {

                /*
                 * Tạo SecretKey từ JWT secret.
                 */
                SecretKey key =
                        Keys.hmacShaKeyFor(
                                secret.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

                /*
                 * Giải mã và kiểm tra chữ ký JWT.
                 */
                Claims claims =
                        Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();

                /*
                 * Lấy thông tin từ JWT.
                 */
                String username =
                        claims.getSubject();

                String role =
                        claims.get(
                                "role",
                                String.class
                        );

                Long userId =
                        claims.get(
                                "userId",
                                Long.class
                        );

                /*
                 * JWT bắt buộc phải có userId và role.
                 */
                if (userId == null) {

                    throw new IllegalArgumentException(
                            "JWT khong co userId"
                    );
                }

                if (role == null) {

                    throw new IllegalArgumentException(
                            "JWT khong co role"
                    );
                }

                /*
                 * Tạo Authentication cho Spring Security.
                 *
                 * credentials = userId
                 * authority = ROLE_CUSTOMER hoặc ROLE_ADMIN
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                userId,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                        );

                /*
                 * Lưu Authentication vào SecurityContext.
                 * Các Controller có thể lấy thông tin
                 * người dùng hiện tại từ Authentication.
                 */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );

            } catch (Exception e) {

                /*
                 * JWT không hợp lệ hoặc đã hết hạn.
                 */
                SecurityContextHolder
                        .clearContext();

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.setContentType(
                        "application/json;charset=UTF-8"
                );

                response.getWriter().write(
                        "{\"message\":\"JWT khong hop le\"}"
                );

                return;
            }
        }

        /*
         * Tiếp tục xử lý request.
         */
        filterChain.doFilter(
                request,
                response
        );
    }
}