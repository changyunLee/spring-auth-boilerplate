package com.example.auth.dto;

import com.example.auth.domain.Role;
import com.example.auth.domain.TwoFactorType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProfileUpdateRequest {
        private String displayName;
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TwoFactorChangeRequest {
        private TwoFactorType twoFactorType;
    }
}
