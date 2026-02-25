package com.example.auth.service;

import com.example.auth.domain.EmailVerificationToken;
import com.example.auth.domain.PasswordResetToken;
import com.example.auth.domain.Provider;
import com.example.auth.domain.RefreshToken;
import com.example.auth.domain.Role;
import com.example.auth.domain.TwoFactorType;
import com.example.auth.domain.User;
import com.example.auth.dto.AuthDto;
import com.example.auth.exception.AccountLockedException;
import com.example.auth.repository.EmailVerificationTokenRepository;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditLogService auditLogService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    // ── 회원가입 ────────────────────────────────────────────────────────────
    @Transactional
    public Long register(AuthDto.SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입되어 있는 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .provider(Provider.LOCAL)
                .build();

        Long userId = userRepository.save(user).getId();

        String token = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .token(token)
                .email(request.getEmail())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        return userId;
    }

    // ── 회원가입 후 인증 이메일 발송 (트랜잭션 외부, SMTP 실패해도 가입은 유지) ──
    public void sendVerificationEmailAfterRegister(String email) {
        try {
            emailVerificationTokenRepository.findByEmail(email).ifPresent(token ->
                    emailService.sendVerificationEmail(email, token.getToken())
            );
        } catch (Exception e) {
            log.warn("[이메일 발송 실패] 회원가입은 완료되었으나 인증 메일 발송 중 오류 발생: email={}, error={}", email, e.getMessage());
        }
    }

    // ── 로그인 ──────────────────────────────────────────────────────────────
    @Transactional
    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일이거나, 비밀번호가 틀렸습니다."));

        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("가입되지 않은 이메일이거나, 비밀번호가 틀렸습니다.");
        }

        if (user.isSuspended()) {
            throw new IllegalArgumentException("정지된 계정입니다.");
        }

        if (user.isLocked()) {
            throw new AccountLockedException("비밀번호 5회 오류로 계정이 30분간 잠겼습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedLoginAttempts();
            auditLogService.log("LOGIN_FAIL", request.getEmail(), "비밀번호 불일치 (실패 누적: " + user.getFailedLoginAttempts() + ")");
            throw new IllegalArgumentException("가입되지 않은 이메일이거나, 비밀번호가 틀렸습니다.");
        }

        user.resetFailedLoginAttempts();

        if (user.getTwoFactorType() == TwoFactorType.EMAIL) {
            String code = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
            user.generateTwoFactorCode(code);
            user.startTwoFactor();
            emailService.send2FACodeEmail(user.getEmail(), code);
            return AuthDto.TokenResponse.requires2FA("EMAIL", user.getEmail(), null);
        }

        if (user.getTwoFactorType() == TwoFactorType.GOOGLE_OTP) {
            user.startTwoFactor();
            // 첫 로그인 성공 전까지 지속적으로 QR 코드를 보여주도록 시크릿 키 반환 (사용자 요청 반영)
            return AuthDto.TokenResponse.requires2FA("GOOGLE_OTP", user.getEmail(), user.getTwoFactorSecret());
        }

        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        String refreshToken = createRefreshToken(user.getEmail());
        auditLogService.log("LOGIN_SUCCESS", user.getEmail(), "provider=LOCAL");

        return new AuthDto.TokenResponse(accessToken, refreshToken);
    }

    // ── 2차 인증 로그인 ─────────────────────────────────────────────────────────
    @Transactional
    public AuthDto.TokenResponse login2FA(AuthDto.TwoFactorLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.verifyTwoFactorPending()) {
            throw new IllegalArgumentException("2차 인증 세션이 만료되었거나 올바르지 않은 접근입니다.");
        }

        if (user.getTwoFactorType() == TwoFactorType.EMAIL) {
            if (!user.verifyTwoFactorCode(request.getCode())) {
                throw new IllegalArgumentException("인증 코드가 틀리거나 만료되었습니다.");
            }
        } else if (user.getTwoFactorType() == TwoFactorType.GOOGLE_OTP) {
            com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
            try {
                int code = Integer.parseInt(request.getCode());
                if (!gAuth.authorize(user.getTwoFactorSecret(), code)) {
                    throw new IllegalArgumentException("G-OTP 코드가 틀렸습니다.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("올바른 코드 형식이 아닙니다.");
            }
        } else {
            throw new IllegalArgumentException("2차 인증이 설정되지 않은 사용자입니다.");
        }

        user.clearTwoFactorPending();

        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        String refreshToken = createRefreshToken(user.getEmail());
        auditLogService.log("LOGIN_2FA_SUCCESS", user.getEmail(), "type=" + user.getTwoFactorType().name());

        return new AuthDto.TokenResponse(accessToken, refreshToken);
    }

    // ── 토큰 갱신 ───────────────────────────────────────────────────────────
    @Transactional
    public AuthDto.TokenResponse refresh(AuthDto.RefreshRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (savedToken.isExpired()) {
            refreshTokenRepository.delete(savedToken);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다. 다시 로그인해주세요.");
        }

        User user = userRepository.findByEmail(savedToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        refreshTokenRepository.delete(savedToken);

        String newAccessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = createRefreshToken(user.getEmail());

        return new AuthDto.TokenResponse(newAccessToken, newRefreshToken);
    }

    // ── 로그아웃 ─────────────────────────────────────────────────────────────
    @Transactional
    public void logout(AuthDto.LogoutRequest request, String accessToken) {
        String tokenHash = hashToken(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            auditLogService.log("LOGOUT", rt.getEmail(), null);
            refreshTokenRepository.delete(rt);
        });
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            tokenBlacklistService.blacklist(accessToken);
        }
    }

    // ── 이메일 인증 ──────────────────────────────────────────────────────────
    @Transactional
    public void verifyEmail(AuthDto.EmailVerificationRequest request) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        if (token.isExpired()) {
            emailVerificationTokenRepository.delete(token);
            throw new IllegalArgumentException("만료된 인증 토큰입니다. 인증 이메일을 재발송해주세요.");
        }

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.verifyEmail();
        emailVerificationTokenRepository.delete(token);
        auditLogService.log("EMAIL_VERIFIED", user.getEmail(), "email verified via token");
    }

    // ── 인증 이메일 재발송 ────────────────────────────────────────────────────
    @Transactional
    public void resendVerification(AuthDto.ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("이미 이메일 인증이 완료된 계정입니다.");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByEmail(request.getEmail()).orElse(null);
        if (verificationToken != null) {
            verificationToken.updateToken(token, expiresAt);
        } else {
            verificationToken = EmailVerificationToken.builder()
                    .token(token)
                    .email(request.getEmail())
                    .expiresAt(expiresAt)
                    .build();
        }

        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(request.getEmail(), token);
    }

    // ── 비밀번호 재설정 이메일 발송 ──────────────────────────────────────────
    @Transactional
    public void forgotPassword(AuthDto.ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

            PasswordResetToken resetToken = passwordResetTokenRepository.findByEmail(request.getEmail()).orElse(null);
            if (resetToken != null) {
                resetToken.updateToken(token, expiresAt);
            } else {
                resetToken = PasswordResetToken.builder()
                        .token(token)
                        .email(request.getEmail())
                        .expiresAt(expiresAt)
                        .build();
            }

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(request.getEmail(), token);
        });
    }

    // ── 비밀번호 재설정 ───────────────────────────────────────────────────────
    @Transactional
    public void resetPassword(AuthDto.ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 재설정 토큰입니다."));

        if (token.isExpired()) {
            passwordResetTokenRepository.delete(token);
            throw new IllegalArgumentException("만료된 재설정 토큰입니다. 다시 요청해주세요.");
        }

        User user = userRepository.findByEmail(token.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        passwordResetTokenRepository.delete(token);
        refreshTokenRepository.findByEmail(user.getEmail()).ifPresent(refreshTokenRepository::delete);
        auditLogService.log("PASSWORD_RESET", user.getEmail(), null);
    }

    // ── OAuth2 토큰 발급 (Google 로그인 성공 후 호출) ─────────────────────────
    @Transactional
    public AuthDto.TokenResponse generateOAuth2Tokens(String email, String role) {
        String accessToken = jwtTokenProvider.createToken(email, role);
        String refreshToken = createRefreshToken(email);
        return new AuthDto.TokenResponse(accessToken, refreshToken);
    }

    // ── Refresh Token 생성 (내부 공통) ───────────────────────────────────────
    private String createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenValidityInSeconds);

        RefreshToken refreshToken = refreshTokenRepository.findByEmail(email).orElse(null);
        if (refreshToken != null) {
            refreshToken.updateToken(tokenHash, expiresAt);
        } else {
            refreshToken = RefreshToken.builder()
                    .tokenHash(tokenHash)
                    .email(email)
                    .expiresAt(expiresAt)
                    .build();
        }

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("토큰 해시 생성 실패", e);
        }
    }
}
