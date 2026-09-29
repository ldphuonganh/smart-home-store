package vn.edu.crs.orderservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.crs.orderservice.security.JwtAuthFilter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
                        // Chỉ payment-service gọi; Gateway không định tuyến /internal/** ra ngoài
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers("/orders/**").authenticated()
                        .anyRequest().denyAll()
                )
                // Thiếu token -> 401 JSON đúng format chung (mặc định Spring trả 403 rỗng)
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now()
                            + "\",\"status\":401,\"error\":\"Unauthorized\","
                            + "\"message\":\"Ban chua dang nhap hoac token khong hop le\","
                            + "\"path\":\"" + request.getRequestURI() + "\"}");
                }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
