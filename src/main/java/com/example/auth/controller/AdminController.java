package com.example.auth.controller;

import com.example.auth.dto.AdminDto;
import com.example.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable Long id, @RequestBody AdminDto.SuspendRequest request) {
        adminService.suspendUser(id, request.getReason());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/unsuspend")
    public ResponseEntity<Void> unsuspendUser(@PathVariable Long id) {
        adminService.unsuspendUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id) {
        adminService.unlockUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @RequestBody AdminDto.RoleChangeRequest request) {
        adminService.changeUserRole(id, request.getRole());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody AdminDto.PasswordResetRequest request) {
        adminService.resetUserPassword(id, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/2fa")
    public ResponseEntity<Void> change2fa(@PathVariable Long id, @RequestBody AdminDto.TwoFactorChangeRequest request) {
        adminService.changeUser2fa(id, request.getTwoFactorType());
        return ResponseEntity.ok().build();
    }
}
