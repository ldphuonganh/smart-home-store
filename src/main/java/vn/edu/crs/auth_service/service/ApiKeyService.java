package vn.edu.crs.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.auth_service.dto.ApiKeyCreateRequestDTO;
import vn.edu.crs.auth_service.dto.ApiKeyResponseDTO;
import vn.edu.crs.auth_service.entity.ApiKey;
import vn.edu.crs.auth_service.repository.ApiKeyRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final String ACTIVE = "ACTIVE";
    private static final String REVOKED = "REVOKED";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ApiKeyRepository apiKeyRepository;

    @Transactional
    public ApiKeyResponseDTO create(ApiKeyCreateRequestDTO dto) {
        ApiKey apiKey = ApiKey.builder()
                .keyValue(generateRandomKey())
                .ownerName(dto.getOwnerName())
                .scopes(dto.getScopes())
                .status(ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(dto.getValidDays() != null ? LocalDateTime.now().plusDays(dto.getValidDays()) : null)
                .build();

        return toDTO(apiKeyRepository.save(apiKey));
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponseDTO> getAll() {
        return apiKeyRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revoke(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy API Key id = " + id));
        apiKey.setStatus(REVOKED);
        apiKeyRepository.save(apiKey);
    }

    @Transactional(readOnly = true)
    public boolean isValidForScope(String keyValue, String requiredScope) {
        return apiKeyRepository.findByKeyValue(keyValue)
                .filter(k -> ACTIVE.equals(k.getStatus()))
                .filter(k -> k.getExpiresAt() == null || k.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(k -> Arrays.stream(k.getScopes().split(","))
                        .map(String::trim)
                        .anyMatch(scope -> scope.equalsIgnoreCase(requiredScope)))
                .isPresent();
    }

    private String generateRandomKey() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return "crs_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ApiKeyResponseDTO toDTO(ApiKey k) {
        return ApiKeyResponseDTO.builder()
                .id(k.getId())
                .keyValue(k.getKeyValue())
                .ownerName(k.getOwnerName())
                .scopes(k.getScopes())
                .status(k.getStatus())
                .expiresAt(k.getExpiresAt())
                .createdAt(k.getCreatedAt())
                .build();
    }
}