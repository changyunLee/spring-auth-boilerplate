-- Spring Auth Boilerplate - Initial Schema
-- V1: 5개 테이블 생성 (users, refresh_tokens, email_verification_tokens, password_reset_tokens, audit_logs)

CREATE TABLE IF NOT EXISTS users (
    id                         BIGINT       NOT NULL AUTO_INCREMENT,
    email                      VARCHAR(100) NOT NULL,
    password                   VARCHAR(100) NULL,
    display_name               VARCHAR(50)  NULL,
    profile_image_url          VARCHAR(500) NULL,
    role                       VARCHAR(20)  NULL,
    provider                   VARCHAR(20)  NULL,
    provider_id                VARCHAR(100) NULL,
    email_verified             TINYINT      NOT NULL DEFAULT 0,
    two_factor_type            VARCHAR(20)  NULL,
    two_factor_verified        TINYINT      NOT NULL DEFAULT 0,
    two_factor_secret          VARCHAR(64)  NULL,
    two_factor_code            VARCHAR(10)  NULL,
    two_factor_code_expires_at DATETIME(6)  NULL,
    two_factor_pending_until   DATETIME(6)  NULL,
    failed_login_attempts      TINYINT      NOT NULL DEFAULT 0,
    locked_until               DATETIME(6)  NULL,
    suspended                  TINYINT      NOT NULL DEFAULT 0,
    suspended_reason           VARCHAR(500) NULL,
    suspended_until            DATETIME(6)  NULL,
    admin_note                 VARCHAR(1000) NULL,
    deleted_at                 DATETIME(6)  NULL,
    created_at                 DATETIME(6)  NULL,
    updated_at                 DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(64) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_refresh_tokens_token_hash (token_hash),
    UNIQUE KEY uq_refresh_tokens_email (email)
);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    token      CHAR(36)    NOT NULL,
    email      VARCHAR(100) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_evt_token (token),
    UNIQUE KEY uq_evt_email (email)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    token      CHAR(36)    NOT NULL,
    email      VARCHAR(100) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_prt_token (token),
    UNIQUE KEY uq_prt_email (email)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    action       VARCHAR(255) NULL,
    target_email VARCHAR(255) NULL,
    ip_address   VARCHAR(255) NULL,
    details      VARCHAR(1000) NULL,
    created_at   DATETIME(6)  NULL,
    PRIMARY KEY (id)
);
