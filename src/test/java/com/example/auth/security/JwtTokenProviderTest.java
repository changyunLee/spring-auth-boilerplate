package com.example.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // Base64 encoded 256-bit secret key for testing
        String secretKey = Base64.getEncoder().encodeToString("testsecretkeytestsecretkeytestse".getBytes());
        long validityInSeconds = 3600; // 1 hour
        jwtTokenProvider = new JwtTokenProvider(secretKey, validityInSeconds);
    }

    @Test
    @DisplayName("토큰 생성 및 검증 테스트")
    void createAndValidateToken() {
        // given
        String email = "test@example.com";
        String role = "ROLE_USER";

        // when
        String token = jwtTokenProvider.createToken(email, role);

        // then
        assertThat(token).isNotBlank();
        assertTrue(jwtTokenProvider.validateToken(token));
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(role);
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패 테스트")
    void validateInvalidToken() {
        // given
        String invalidToken = "invalid.token.value";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertFalse(isValid);
    }
}
