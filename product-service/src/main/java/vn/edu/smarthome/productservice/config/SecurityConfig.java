package vn.edu.smarthome.productservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.smarthome.productservice.exception.ErrorResponses;
import vn.edu.smarthome.productservice.security.JwtAuthFilter;

/**
 * Phân quyền của product-service.
 * Service TỰ xác thực JWT (không tin tưởng mù quáng Gateway - Buổi 4).
 *
 * - GET /products/**, /categories/**  : public (khách chưa đăng nhập vẫn xem được)
 * - /public/products/**                : public ở tầng service; API Key + scope kiểm tra tại Gateway
 * - /internal/**                       : chỉ service nội bộ gọi; Gateway không định tuyến ra ngoài
 *                                        (giới hạn đã biết - xem docs/API_CONTRACT.md)
 * - POST /products/{id}/reviews        : CUSTOMER (đã mua & nhận hàng)
 * - GET /products/admin, /products/reviews và POST/PUT/PATCH/DELETE: chỉ ADMIN
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/admin", "/products/reviews").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/products/*/reviews/eligibility", "/products/reviews/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products/*/reviews").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/products/**", "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**", "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/products/**", "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**", "/categories/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // Trả lỗi JSON đúng format chung thay vì trang lỗi mặc định
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                ErrorResponses.write(response, HttpStatus.UNAUTHORIZED,
                                        "Bạn chưa đăng nhập hoặc token không hợp lệ", request.getRequestURI()))
                        .accessDeniedHandler((request, response, e) ->
                                ErrorResponses.write(response, HttpStatus.FORBIDDEN,
                                        "Bạn không có quyền thực hiện thao tác này", request.getRequestURI()))
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
