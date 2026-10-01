package vn.edu.smarthome.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.authservice.dto.UserResponse;
import vn.edu.smarthome.authservice.entity.User;
import vn.edu.smarthome.authservice.exception.BadRequestException;
import vn.edu.smarthome.authservice.repository.UserRepository;

import java.util.List;

/** Chức năng quản lý tài khoản phía Admin. */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(AuthService::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateStatus(Long targetId, boolean enabled, Long currentAdminId) {
        if (targetId.equals(currentAdminId) && !enabled) {
            throw new BadRequestException("Không thể tự khoá tài khoản của chính mình");
        }
        User user = authService.findUser(targetId);
        user.setEnabled(enabled);
        return AuthService.toResponse(userRepository.save(user));
    }
}
