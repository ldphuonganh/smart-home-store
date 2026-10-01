package vn.edu.smarthome.authservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.edu.smarthome.authservice.exception.ErrorResponses;
import vn.edu.smarthome.authservice.security.JwtAuthFilter;

/**
 * Đường dẫn NỘI BỘ của service (Gateway rewrite /api/auth/** -> /auth/**,
 * /api/admin/** -> /admin/**, /api/api-keys/** -> /api-keys/**).
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
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        // Gateway gọi để kiểm tra API Key của Partner (không định tuyến ra ngoài)
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers("/admin/**", "/api-keys/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
