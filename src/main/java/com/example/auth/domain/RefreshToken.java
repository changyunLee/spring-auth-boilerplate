package com.example.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Comment("리프레시 토큰 저장 테이블 (SHA-256 해시값만 저장)")
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Comment("리프레시 토큰 고유 식별자 (PK, AUTO_INCREMENT)")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("SHA-256 해시된 리프레시 토큰 값 (64자 고정)")
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Comment("토큰 소유자 이메일 (1인 1토큰)")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Comment("토큰 만료 일시")
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Comment("토큰 발급 일시")
    @CreatedDate
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void updateToken(String tokenHash, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}
