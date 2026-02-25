package com.example.auth.dto;

import com.example.auth.domain.Role;
import com.example.auth.domain.TwoFactorType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SuspendRequest {
        private String reason;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RoleChangeRequest {
        private Role role;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PasswordResetRequest {
        private String newPassword;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TwoFactorChangeRequest {
        private TwoFactorType twoFactorType;
    }
}
