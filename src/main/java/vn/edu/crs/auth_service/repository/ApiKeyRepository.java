package vn.edu.crs.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.crs.auth_service.entity.ApiKey;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    // 1. Tìm API Key theo chuỗi key_value
    Optional<ApiKey> findByKeyValue(String keyValue);

    // 2. Tìm API Key theo key_value và trạng thái (chỉ lấy key đang ACTIVE)
    Optional<ApiKey> findByKeyValueAndStatus(String keyValue, String status);

    // 3. Kiểm tra xem chuỗi key_value đã tồn tại trong CSDL chưa
    boolean existsByKeyValue(String keyValue);
}