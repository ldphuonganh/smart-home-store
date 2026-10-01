package vn.edu.smarthome.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.authservice.dto.*;
import vn.edu.smarthome.authservice.entity.Role;
import vn.edu.smarthome.authservice.entity.User;
import vn.edu.smarthome.authservice.exception.ConflictException;
import vn.edu.smarthome.authservice.exception.ForbiddenException;
import vn.edu.smarthome.authservice.exception.ResourceNotFoundException;
import vn.edu.smarthome.authservice.exception.UnauthorizedException;
import vn.edu.smarthome.authservice.repository.UserRepository;
import vn.edu.smarthome.authservice.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email đã được sử dụng");
        }
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(blankToNull(request.getPhone()));
        user.setRole(Role.CUSTOMER); // tự đăng ký luôn là CUSTOMER
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new UnauthorizedException("Email hoặc mật khẩu không chính xác"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email hoặc mật khẩu không chính xác");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ForbiddenException("Tài khoản đã bị khoá, vui lòng liên hệ quản trị viên");
        }
        return new LoginResponse(jwtService.generateToken(user), "Bearer", toResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = findUser(id);
        user.setFullName(request.getFullName().trim());
        user.setPhone(blankToNull(request.getPhone()));
        return toResponse(userRepository.save(user));
    }

    User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản id = " + id));
    }

    static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
