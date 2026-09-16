package vn.edu.crs.auth_service.controller;

import vn.edu.crs.auth_service.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/internal/api-keys")
@RequiredArgsConstructor
public class InternalApiKeyController {
    private final ApiKeyService apiKeyService;

    @GetMapping("/validate")
    public Map<String, Object> validate(@RequestParam String key, @RequestParam String scope) {
        boolean valid = apiKeyService.isValidForScope(key, scope);
        return Map.of("valid", valid);
    }
}