package com.example.auth.dto;

import com.example.auth.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDto {

    // ── Request DTOs ──────────────────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserSearchRequest {
        private String email;
        private Role role;
        private String provider;
        private Boolean emailVerified;
        private Boolean isLocked;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SuspendRequest {
        private String reason;
        private LocalDateTime suspendedUntil;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RoleChangeRequest {
        private Role role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PasswordResetRequest {
        private String newPassword;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TwoFactorChangeRequest {
        private String twoFactorType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AdminNoteRequest {
        private String note;
    }

    // ── Response DTOs ─────────────────────────────────────────────────────

    @Getter
    @AllArgsConstructor
    public static class DashboardStats {
        private final long totalUsers;
        private final long adminCount;
        private final long userCount;
        private final long verifiedCount;
        private final long lockedCount;
        private final long localCount;
        private final long googleCount;
        private final long newThisWeek;
    }

    @Getter
    @AllArgsConstructor
    public static class DailyCount {
        private final LocalDate date;
        private final long count;
    }

    @Getter
    @AllArgsConstructor
    public static class RegistrationTrendResponse {
        private final int days;
        private final List<DailyCount> data;
    }

    @Getter
    @AllArgsConstructor
    public static class DailyLoginCount {
        private final LocalDate date;
        private final long successCount;
        private final long failCount;
    }

    @Getter
    @AllArgsConstructor
    public static class LoginTrendResponse {
        private final int days;
        private final List<DailyLoginCount> data;
    }

    @Getter
    @AllArgsConstructor
    public static class SecuritySummary {
        private final long lockedAccounts;
        private final long suspendedAccounts;
        private final long recentLoginFailures;
        private final long suspiciousIps;
    }

    @Getter
    @AllArgsConstructor
    public static class TotpSetupResponse {
        private final String secret;
        private final String qrCodeBase64;
    }

    @Getter
    @AllArgsConstructor
    public static class SuspendResponse {
        private final Long id;
        private final String email;
        private final boolean suspended;
        private final String suspendedReason;
        private final LocalDateTime suspendedUntil;
    }

    @Getter
    @AllArgsConstructor
    public static class ActiveSessionResponse {
        private final String email;
        private final LocalDateTime createdAt;
        private final LocalDateTime expiresAt;
    }

    @Getter
    @AllArgsConstructor
    public static class SuspiciousIpResponse {
        private final String ip;
        private final long count;
        private final LocalDateTime lastSeen;
    }

    @Getter
    @AllArgsConstructor
    public static class FailedLoginSummary {
        private final int hours;
        private final long totalFailures;
        private final long distinctIpCount;
        private final long distinctEmailCount;
    }
}
