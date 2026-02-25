package com.example.auth.service;

import com.example.auth.domain.BlacklistedToken;
import com.example.auth.repository.BlacklistedTokenRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Access Token 블랙리스트 서비스.
 *
 * Caffeine(L1) + DB(L2) 2단계 캐시 구조:
 * - L1(Caffeine): 빠른 조회 (메모리)
 * - L2(DB): 재시작 후에도 블랙리스트 유지 (영속성)
 *
 * 보안: raw token 대신 SHA-256 해시만 저장
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Value("${app.jwt.access-expiration}")
    private long accessExpirationMs;

    Cache<String, Boolean> cache;

    @PostConstruct
    void initCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(accessExpirationMs, TimeUnit.MILLISECONDS)
                .maximumSize(10_000)
                .build();
    }

    @Transactional
    public void blacklist(String token) {
        String hash = hashToken(token);
        cache.put(hash, Boolean.TRUE);

        if (!blacklistedTokenRepository.existsByTokenHash(hash)) {
            LocalDateTime expiresAt = LocalDateTime.now().plus(accessExpirationMs, ChronoUnit.MILLIS);
            blacklistedTokenRepository.save(BlacklistedToken.builder()
                    .tokenHash(hash)
                    .expiresAt(expiresAt)
                    .build());
        }
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        String hash = hashToken(token);

        if (Boolean.TRUE.equals(cache.getIfPresent(hash))) {
            return true;
        }

        boolean inDb = blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(hash, LocalDateTime.now());
        if (inDb) {
            cache.put(hash, Boolean.TRUE);
        }
        return inDb;
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
