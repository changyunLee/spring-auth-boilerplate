package com.example.auth.controller;

import com.example.auth.dto.AdminDto;
import com.example.auth.dto.UserDto;
import com.example.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto.UserResponse>> searchUsers(
            AdminDto.UserSearchRequest request, Pageable pageable) {
        return ResponseEntity.ok(adminService.searchUsers(request, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetail(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDto.DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/stats/registrations")
    public ResponseEntity<AdminDto.RegistrationTrendResponse> getRegistrationTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(adminService.getRegistrationTrend(days));
    }

    @GetMapping("/stats/logins")
    public ResponseEntity<AdminDto.LoginTrendResponse> getLoginTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(adminService.getLoginTrend(days));
    }

    @GetMapping("/security/summary")
    public ResponseEntity<AdminDto.SecuritySummary> getSecuritySummary() {
        return ResponseEntity.ok(adminService.getSecuritySummary());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Void> changeUserRole(
            @PathVariable Long id, @RequestBody AdminDto.RoleChangeRequest request) {
        adminService.changeUserRole(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/2fa")
    public ResponseEntity<AdminDto.TotpSetupResponse> changeUserTwoFactor(
            @PathVariable Long id, @RequestBody AdminDto.TwoFactorChangeRequest request) {
        return ResponseEntity.ok(adminService.changeUserTwoFactor(id, request));
    }

    @GetMapping("/users/{id}/2fa-qr")
    public ResponseEntity<AdminDto.TotpSetupResponse> getUserTwoFactorQr(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserTwoFactorQr(id));
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id) {
        adminService.unlockUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<AdminDto.SuspendResponse> suspendUser(
            @PathVariable Long id, @RequestBody AdminDto.SuspendRequest request) {
        return ResponseEntity.ok(adminService.suspendUser(id, request));
    }

    @PutMapping("/users/{id}/unsuspend")
    public ResponseEntity<AdminDto.SuspendResponse> unsuspendUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.unsuspendUser(id));
    }

    @PostMapping("/users/{id}/force-logout")
    public ResponseEntity<Void> forceLogout(@PathVariable Long id) {
        adminService.forceLogout(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/note")
    public ResponseEntity<Void> updateAdminNote(
            @PathVariable Long id, @RequestBody AdminDto.AdminNoteRequest request) {
        adminService.updateAdminNote(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<Page<AdminDto.ActiveSessionResponse>> getActiveSessions(Pageable pageable) {
        return ResponseEntity.ok(adminService.getActiveSessions(pageable));
    }

    @DeleteMapping("/sessions/{email}")
    public ResponseEntity<Void> revokeSession(@PathVariable String email) {
        adminService.revokeSession(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/security/suspicious-ips")
    public ResponseEntity<List<AdminDto.SuspiciousIpResponse>> getSuspiciousIps(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(adminService.getSuspiciousIps(hours));
    }

    @GetMapping("/security/failed-logins")
    public ResponseEntity<AdminDto.FailedLoginSummary> getFailedLoginSummary(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(adminService.getFailedLoginSummary(hours));
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable Long id, @RequestBody AdminDto.PasswordResetRequest request) {
        adminService.resetUserPassword(id, request);
        return ResponseEntity.ok().build();
    }
}
