package com.example.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Comment("블랙리스트된 액세스 토큰 저장 테이블 (서버 재시작 후에도 무효화 유지)")
@Entity
@Table(name = "blacklisted_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BlacklistedToken {

    @Comment("블랙리스트 토큰 고유 식별자 (PK, AUTO_INCREMENT)")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("SHA-256 해시된 액세스 토큰 값 (64자 고정, 중복 차단)")
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Comment("토큰 원본 만료 일시 (이 시각 이후 자동 정리 대상)")
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Comment("블랙리스트 등록 일시")
    @CreatedDate
    private LocalDateTime createdAt;
}
