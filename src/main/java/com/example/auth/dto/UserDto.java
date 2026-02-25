package com.example.auth.dto;

import com.example.auth.domain.Provider;
import com.example.auth.domain.Role;
import com.example.auth.domain.TwoFactorType;
import com.example.auth.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class UserDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProfileUpdateRequest {
        private String displayName;
        private String profileImageUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TwoFactorChangeRequest {
        private TwoFactorType twoFactorType;
    }

    @Getter
    public static class UserResponse {
        private final Long id;
        private final String email;
        private final String displayName;
        private final String profileImageUrl;
        private final Role role;
        private final Provider provider;
        private final boolean emailVerified;
        private final TwoFactorType twoFactorType;
        private final int failedLoginAttempts;
        private final LocalDateTime lockedUntil;
        private final boolean locked;
        private final boolean suspended;
        private final String suspendedReason;
        private final LocalDateTime suspendedUntil;
        private final String adminNote;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        private UserResponse(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.displayName = user.getDisplayName();
            this.profileImageUrl = user.getProfileImageUrl();
            this.role = user.getRole();
            this.provider = user.getProvider();
            this.emailVerified = user.isEmailVerified();
            this.twoFactorType = user.getTwoFactorType();
            this.failedLoginAttempts = user.getFailedLoginAttempts();
            this.lockedUntil = user.getLockedUntil();
            this.locked = user.isLocked();
            this.suspended = user.isSuspended();
            this.suspendedReason = user.getSuspendedReason();
            this.suspendedUntil = user.getSuspendedUntil();
            this.adminNote = user.getAdminNote();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
        }

        public static UserResponse from(User user) {
            return new UserResponse(user);
        }
    }
}
