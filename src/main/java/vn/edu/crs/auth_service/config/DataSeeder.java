package vn.edu.crs.auth_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.crs.auth_service.entity.User;
import vn.edu.crs.auth_service.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Tạo tài khoản ADMIN mặc định nếu chưa có
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@crs.edu.vn")
                    .fullName("System Administrator")
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
        }

        // 2. Tạo tài khoản CUSTOMER mặc định nếu chưa có
        if (!userRepository.existsByUsername("customer1")) {
            User customer = User.builder()
                    .username("customer1")
                    .password(passwordEncoder.encode("customer123"))
                    .email("customer1@crs.edu.vn")
                    .fullName("Nguyen Van Customer")
                    .role("CUSTOMER")
                    .build();
            userRepository.save(customer);
        }
    }
}