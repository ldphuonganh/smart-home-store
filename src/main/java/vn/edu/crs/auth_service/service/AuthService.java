package vn.edu.crs.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.auth_service.dto.LoginRequestDTO;
import vn.edu.crs.auth_service.dto.LoginResponseDTO;
import vn.edu.crs.auth_service.dto.RegisterRequestDTO;
import vn.edu.crs.auth_service.entity.User;
import vn.edu.crs.auth_service.exception.InvalidCredentialsException;
import vn.edu.crs.auth_service.repository.UserRepository;
import vn.edu.crs.auth_service.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 1. Xử lý Đăng nhập
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        return LoginResponseDTO.builder()
                .userId(user.getId())
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    // 2. Xử lý Đăng ký tài khoản mới
    @Transactional
    public User register(RegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User newUser = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(dto.getRole() != null ? dto.getRole() : "CUSTOMER")
                .build();

        return userRepository.save(newUser);
    }

    // 3. Lấy thông tin user hiện tại từ Username (Phục vụ endpoint /me)
    @Transactional(readOnly = true)
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }
}