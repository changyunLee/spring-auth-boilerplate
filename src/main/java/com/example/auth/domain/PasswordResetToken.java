package com.example.auth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Comment("비밀번호 재설정 토큰 저장 테이블 (1시간 유효, 사용 후 즉시 삭제)")
@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken {

    @Comment("비밀번호 재설정 토큰 고유 식별자 (PK, AUTO_INCREMENT)")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("UUID 형식의 재설정 토큰 값 (항상 36자 고정, 유니크)")
    @Column(nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private String token;

    @Comment("재설정 대상 이메일 주소 (1인 1토큰)")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Comment("토큰 만료 일시 (발급 후 1시간)")
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public PasswordResetToken(String token, String email, LocalDateTime expiresAt) {
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
    }

    public void updateToken(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
