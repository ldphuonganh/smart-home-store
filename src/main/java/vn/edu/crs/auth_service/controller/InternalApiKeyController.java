package vn.edu.crs.auth_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.auth_service.service.ApiKeyService;

@RestController
@RequestMapping("/internal/api-keys")
@RequiredArgsConstructor
public class InternalApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateKey(
            @RequestParam("key") String keyValue,
            @RequestParam("scope") String scope) {
        return ResponseEntity.ok(apiKeyService.isValidForScope(keyValue, scope));
    }
}