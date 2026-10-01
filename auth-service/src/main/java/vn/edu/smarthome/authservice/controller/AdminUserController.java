package vn.edu.smarthome.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.smarthome.authservice.dto.UpdateUserStatusRequest;
import vn.edu.smarthome.authservice.dto.UserResponse;
import vn.edu.smarthome.authservice.security.AuthUser;
import vn.edu.smarthome.authservice.service.UserAdminService;

import java.util.List;

/** Qua Gateway: /api/admin/users/** -> /admin/users/** (chỉ ADMIN). */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAdminService userAdminService;

    @GetMapping
    public List<UserResponse> getAll() {
        return userAdminService.getAll();
    }

    @PutMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody UpdateUserStatusRequest request,
                                     @AuthenticationPrincipal AuthUser currentUser) {
        return userAdminService.updateStatus(id, request.getEnabled(), currentUser.id());
    }
}
