package vn.edu.smarthome.paymentservice.config;

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
import vn.edu.smarthome.paymentservice.exception.ErrorResponses;
import vn.edu.smarthome.paymentservice.security.JwtAuthFilter;

/** Qua Gateway: /api/payments/** -> /payments/**. */
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
                        // Danh sách toàn bộ thanh toán: chỉ ADMIN
                        .requestMatchers(HttpMethod.GET, "/payments").hasRole("ADMIN")
                        // Tạo / xác nhận / huỷ thanh toán: chỉ CUSTOMER (chủ đơn)
                        .requestMatchers(HttpMethod.POST, "/payments/**").hasRole("CUSTOMER")
                        .requestMatchers("/payments/**").authenticated()
                        .anyRequest().denyAll()
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
}
