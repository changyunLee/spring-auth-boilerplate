package com.example.auth.controller;

import com.example.auth.dto.AuthDto;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증/권한 관련 API (회원가입, 로그인, 로그아웃)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 신규 회원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Validated @RequestBody AuthDto.SignUpRequest request) {
        String email = request.getEmail();
        authService.register(request);
        // 트랜잭션 커밋 후 이메일 발송 (SMTP 실패가 가입 자체를 막지 않도록 분리)
        authService.sendVerificationEmailAfterRegister(email);
        return ResponseEntity.ok("회원가입이 완료되었습니다. 이메일 인증 후 로그인하세요.");
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인 후 Access Token과 Refresh Token을 반환하거나, 2차 인증을 요구합니다.")
    @PostMapping("/login")
    public ResponseEntity<AuthDto.TokenResponse> login(@Validated @RequestBody AuthDto.LoginRequest request) {
        AuthDto.TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "2차 인증 로그인", description = "이메일 또는 구글 OTP 인증 코드를 검증하고 토큰을 발급합니다.")
    @PostMapping("/login/2fa")
    public ResponseEntity<AuthDto.TokenResponse> login2FA(@Validated @RequestBody AuthDto.TwoFactorLoginRequest request) {
        AuthDto.TokenResponse tokenResponse = authService.login2FA(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthDto.TokenResponse> refresh(@Validated @RequestBody AuthDto.RefreshRequest request) {
        AuthDto.TokenResponse tokenResponse = authService.refresh(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 Access Token을 즉시 무효화합니다.")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Validated @RequestBody AuthDto.LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        authService.logout(request, accessToken);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @Operation(summary = "이메일 인증", description = "이메일로 발송된 토큰으로 계정을 인증합니다.")
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Validated @RequestBody AuthDto.EmailVerificationRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    @Operation(summary = "인증 이메일 재발송", description = "이메일 인증 링크를 재발송합니다.")
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@Validated @RequestBody AuthDto.ResendVerificationRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok("인증 이메일이 재발송되었습니다.");
    }

    @Operation(summary = "비밀번호 재설정 요청", description = "입력한 이메일로 비밀번호 재설정 링크를 발송합니다.")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Validated @RequestBody AuthDto.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("비밀번호 재설정 이메일이 발송되었습니다.");
    }

    @Operation(summary = "비밀번호 재설정", description = "재설정 토큰과 새 비밀번호로 비밀번호를 변경합니다.")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Validated @RequestBody AuthDto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("비밀번호가 재설정되었습니다.");
    }
}
