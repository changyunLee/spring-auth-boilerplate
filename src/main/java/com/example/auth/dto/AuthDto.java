package com.example.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.example.auth.validation.ValidPassword;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SignUpRequest {
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식에 맞지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @ValidPassword
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RefreshRequest {
        @NotBlank(message = "리프레시 토큰은 필수 입력 값입니다.")
        private String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LogoutRequest {
        @NotBlank(message = "리프레시 토큰은 필수 입력 값입니다.")
        private String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EmailVerificationRequest {
        @NotBlank(message = "토큰은 필수 입력 값입니다.")
        private String token;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResendVerificationRequest {
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식에 맞지 않습니다.")
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식에 맞지 않습니다.")
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "토큰은 필수 입력 값입니다.")
        private String token;

        @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
        @ValidPassword
        private String newPassword;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TwoFactorLoginRequest {
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        private String email;

        @NotBlank(message = "인증 코드는 필수 입력 값입니다.")
        private String code;
    }

    @Getter
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String status;
        private String twoFactorType;
        private String email;
        private String twoFactorSecret;

        public TokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.status = "SUCCESS";
        }

        public static TokenResponse requires2FA(String twoFactorType, String email, String secret) {
            TokenResponse res = new TokenResponse(null, null);
            res.status = "REQUIRES_2FA";
            res.twoFactorType = twoFactorType;
            res.email = email;
            res.twoFactorSecret = secret;
            return res;
        }
    }
}
