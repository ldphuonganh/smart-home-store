package vn.edu.smarthome.authservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.smarthome.authservice.entity.Role;
import vn.edu.smarthome.authservice.entity.User;
import vn.edu.smarthome.authservice.repository.UserRepository;

/**
 * Tài khoản mẫu để demo (tạo nếu chưa có):
 *   ADMIN    : admin@smarthome.vn    / admin123
 *   CUSTOMER : customer@smarthome.vn / customer123
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        create("Quản trị viên", "admin@smarthome.vn", "admin123", "0900000001", Role.ADMIN);
        create("Nguyễn Văn A", "customer@smarthome.vn", "customer123", "0912345678", Role.CUSTOMER);
    }

    private void create(String fullName, String email, String password, String phone, Role role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role);
        userRepository.save(user);
        log.info("Đã tạo tài khoản mẫu {} ({})", email, role);
    }
}
