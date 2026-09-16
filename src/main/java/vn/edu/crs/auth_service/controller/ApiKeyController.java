package vn.edu.crs.auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.auth_service.dto.ApiKeyCreateRequestDTO;
import vn.edu.crs.auth_service.dto.ApiKeyResponseDTO;
import vn.edu.crs.auth_service.service.ApiKeyService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<ApiKeyResponseDTO> createKey(@Valid @RequestBody ApiKeyCreateRequestDTO dto) {
        return ResponseEntity.ok(apiKeyService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponseDTO>> getAllKeys() {
        return ResponseEntity.ok(apiKeyService.getAll());
    }

    @PutMapping("/{id}/revoke")
    public ResponseEntity<Void> revokeKey(@PathVariable Long id) {
        apiKeyService.revoke(id);
        return ResponseEntity.noContent().build();
    }
}