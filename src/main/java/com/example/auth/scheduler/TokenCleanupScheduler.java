package com.example.auth.scheduler;

import com.example.auth.repository.BlacklistedTokenRepository;
import com.example.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.example.auth.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${audit.log.retention-days:90}")
    private int auditLogRetentionDays;

    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("만료된 토큰 및 오래된 감사 로그 정리 시작...");

        int countRt = refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        int countBt = blacklistedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        LocalDateTime cutoff = LocalDateTime.now().minusDays(auditLogRetentionDays);
        int countLogs = auditLogRepository.deleteByCreatedAtBefore(cutoff);

        log.info("완료: RT {} 건, 블랙리스트 AT {} 건, 감사 로그 {} 건 삭제.", countRt, countBt, countLogs);
    }
}
