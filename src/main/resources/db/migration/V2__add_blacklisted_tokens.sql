-- V2: Access Token 블랙리스트 테이블 추가
-- Phase 5-D: Caffeine(L1) + DB(L2) 2단계 캐시로 재시작 후에도 블랙리스트 유지

CREATE TABLE IF NOT EXISTS blacklisted_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_blacklisted_tokens_hash (token_hash),
    INDEX idx_blacklisted_tokens_expires_at (expires_at)
);
