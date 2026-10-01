package vn.edu.smarthome.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.authservice.service.ApiKeyService;

import java.util.Map;

/**
 * API nội bộ cho api-gateway kiểm tra X-API-KEY của Partner.
 * Trả { "valid": true/false } đúng như AuthServiceClient ở Gateway đang đọc.
 */
@RestController
@RequestMapping("/internal/api-keys")
@RequiredArgsConstructor
public class InternalApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping("/validate")
    public Map<String, Boolean> validate(@RequestParam String key, @RequestParam String scope) {
        return Map.of("valid", apiKeyService.isValidForScope(key, scope));
    }
}
