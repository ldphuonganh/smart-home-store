package vn.edu.smarthome.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smarthome.authservice.dto.ApiKeyCreateRequest;
import vn.edu.smarthome.authservice.dto.ApiKeyResponse;
import vn.edu.smarthome.authservice.entity.ApiKey;
import vn.edu.smarthome.authservice.exception.ResourceNotFoundException;
import vn.edu.smarthome.authservice.repository.ApiKeyRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/** Quản lý thông tin Partner + API Key + Scope. */
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;

    @Transactional
    public ApiKeyResponse create(ApiKeyCreateRequest request) {
        ApiKey apiKey = new ApiKey();
        apiKey.setKeyValue(generateKey());
        apiKey.setOwnerName(request.getOwnerName().trim());
        apiKey.setContactEmail(request.getContactEmail());
        apiKey.setScopes(normalizeScopes(request.getScopes()));
        apiKey.setStatus(ApiKey.ACTIVE);
        if (request.getValidDays() != null) {
            apiKey.setExpiresAt(LocalDateTime.now().plusDays(request.getValidDays()));
        }
        return toResponse(apiKeyRepository.save(apiKey));
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getAll() {
        return apiKeyRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ApiKeyResponse revoke(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy API Key id = " + id));
        apiKey.setStatus(ApiKey.REVOKED);
        return toResponse(apiKeyRepository.save(apiKey));
    }

    /** Gateway gọi: key còn ACTIVE, chưa hết hạn và có scope yêu cầu. */
    @Transactional(readOnly = true)
    public boolean isValidForScope(String keyValue, String requiredScope) {
        return apiKeyRepository.findByKeyValue(keyValue)
                .filter(k -> ApiKey.ACTIVE.equals(k.getStatus()))
                .filter(k -> k.getExpiresAt() == null || k.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(k -> Arrays.stream(k.getScopes().split(","))
                        .map(String::trim)
                        .anyMatch(scope -> scope.equalsIgnoreCase(requiredScope)))
                .isPresent();
    }

    private String normalizeScopes(String scopes) {
        return String.join(",", Arrays.stream(scopes.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList());
    }

    private String generateKey() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return "shp_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ApiKeyResponse toResponse(ApiKey k) {
        String status = k.getStatus();
        if (ApiKey.ACTIVE.equals(status) && k.getExpiresAt() != null
                && k.getExpiresAt().isBefore(LocalDateTime.now())) {
            status = "EXPIRED";
        }
        return ApiKeyResponse.builder()
                .id(k.getId())
                .keyValue(k.getKeyValue())
                .ownerName(k.getOwnerName())
                .contactEmail(k.getContactEmail())
                .scopes(k.getScopes())
                .status(status)
                .expiresAt(k.getExpiresAt())
                .createdAt(k.getCreatedAt())
                .build();
    }
}
