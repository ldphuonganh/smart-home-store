package vn.edu.smarthome.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.authservice.dto.ApiKeyCreateRequest;
import vn.edu.smarthome.authservice.dto.ApiKeyResponse;
import vn.edu.smarthome.authservice.service.ApiKeyService;

import java.util.List;

/** Quản lý Partner / API Key. Qua Gateway: /api/api-keys/** -> /api-keys/** (chỉ ADMIN). */
@RestController
@RequestMapping("/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public List<ApiKeyResponse> getAll() {
        return apiKeyService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyResponse create(@Valid @RequestBody ApiKeyCreateRequest request) {
        return apiKeyService.create(request);
    }

    /** Thu hồi key (không xoá hẳn để còn lịch sử). */
    @DeleteMapping("/{id}")
    public ApiKeyResponse revoke(@PathVariable Long id) {
        return apiKeyService.revoke(id);
    }
}
