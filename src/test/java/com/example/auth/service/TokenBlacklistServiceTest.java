package com.example.auth.service;

import com.example.auth.domain.BlacklistedToken;
import com.example.auth.repository.BlacklistedTokenRepository;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenBlacklistService 통합 테스트.
 *
 * Phase 5-D: Caffeine(L1) + DB(L2) 2단계 캐시 검증.
 * - DB 영속성: 서버 재시작 후에도 블랙리스트 유지
 * - 캐시 우선 조회: DB 불필요 요청 최소화
 */
@SpringBootTest
@ActiveProfiles("test")
class TokenBlacklistServiceTest {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @MockBean
    private JavaMailSender javaMailSender;

    @AfterEach
    void tearDown() {
        blacklistedTokenRepository.deleteAll();
        clearCache();
    }

    @Test
    @DisplayName("블랙리스트 등록 시 SHA-256 해시가 DB에 저장된다")
    void blacklistPersistsTokenHashToDatabase() {
        // given
        String token = "eyJhbGciOiJIUzI1NiJ9.test.payload";

        // when
        tokenBlacklistService.blacklist(token);

        // then
        List<BlacklistedToken> all = blacklistedTokenRepository.findAll();
        assertThat(all).hasSize(1);

        BlacklistedToken saved = all.get(0);
        assertThat(saved.getTokenHash()).hasSize(64);           // SHA-256 hex = 64자
        assertThat(saved.getTokenHash()).isNotEqualTo(token);   // raw token 저장 금지
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("블랙리스트에 등록된 토큰은 isBlacklisted가 true를 반환한다")
    void isBlacklistedReturnsTrueForBlacklistedToken() {
        // given
        String token = "eyJhbGciOiJIUzI1NiJ9.test.payload";
        tokenBlacklistService.blacklist(token);

        // when & then
        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();
    }

    @Test
    @DisplayName("블랙리스트에 없는 토큰은 isBlacklisted가 false를 반환한다")
    void isBlacklistedReturnsFalseForUnknownToken() {
        // given
        String token = "eyJhbGciOiJIUzI1NiJ9.unknown.token";

        // when & then
        assertThat(tokenBlacklistService.isBlacklisted(token)).isFalse();
    }

    @Test
    @DisplayName("캐시 제거 후에도 DB에서 조회하여 isBlacklisted가 true를 반환한다 (재시작 시뮬레이션)")
    void isBlacklistedReturnsTrueAfterCacheEviction() {
        // given
        String token = "eyJhbGciOiJIUzI1NiJ9.test.payload";
        tokenBlacklistService.blacklist(token);

        // when - 캐시 비우기 (서버 재시작 시뮬레이션)
        clearCache();

        // then - DB에서 조회하여 true 반환
        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();
    }

    @Test
    @DisplayName("동일 토큰을 두 번 블랙리스트 등록해도 DB에는 1건만 저장된다")
    void doubleBlacklistIsIdempotent() {
        // given
        String token = "eyJhbGciOiJIUzI1NiJ9.test.payload";

        // when
        tokenBlacklistService.blacklist(token);
        tokenBlacklistService.blacklist(token);

        // then
        assertThat(blacklistedTokenRepository.count()).isEqualTo(1);
    }

    // ── helper ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void clearCache() {
        Cache<String, Boolean> cache =
                (Cache<String, Boolean>) ReflectionTestUtils.getField(tokenBlacklistService, "cache");
        if (cache != null) {
            cache.invalidateAll();
        }
    }
}
