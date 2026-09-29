package vn.edu.smarthome.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.smarthome.authservice.entity.ApiKey;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyValue(String keyValue);

    List<ApiKey> findAllByOrderByIdDesc();
}
