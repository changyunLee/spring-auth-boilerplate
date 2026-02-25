package com.example.auth.service;

import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import com.example.auth.dto.AuthDto;
import com.example.auth.repository.EmailVerificationTokenRepository;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import com.example.auth.service.AuditLogService;
import com.example.auth.service.TokenBlacklistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import com.example.auth.domain.EmailVerificationToken;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void register_Success() {
        // given
        AuthDto.SignUpRequest request = new AuthDto.SignUpRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

        User savedUser = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 1L);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        assertDoesNotThrow(() -> authService.register(request));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void register_Fail_DuplicateEmail() {
        // given
        AuthDto.SignUpRequest request = new AuthDto.SignUpRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_Success() {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();
        
        ReflectionTestUtils.setField(authService, "refreshTokenValidityInSeconds", 604800L);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createToken(user.getEmail(), user.getRole().name())).willReturn("accessToken");
        given(refreshTokenRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when
        AuthDto.TokenResponse response = authService.login(request);

        // then
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("accessToken", response.getAccessToken());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 카운트 및 잠금")
    void login_Fail_WrongPassword_Lockdown() {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        User user = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        }

        // 6th attempt should throw AccountLockedException
        assertThrows(com.example.auth.exception.AccountLockedException.class, () -> authService.login(request));
        assertTrue(user.isLocked());
    }
}
