package vn.edu.crs.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.crs.auth_service.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Tìm thông tin User theo Username (Phục vụ Login / LoadUser)
    Optional<User> findByUsername(String username);

    // 2. Kiểm tra xem Username đã tồn tại trong CSDL chưa
    boolean existsByUsername(String username);

    // 3. Kiểm tra xem Email đã được đăng ký chưa
    boolean existsByEmail(String email);
}