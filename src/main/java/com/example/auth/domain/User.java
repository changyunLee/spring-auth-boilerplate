package com.example.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Comment("사용자 계정 테이블")
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Comment("사용자 고유 식별자 (PK, AUTO_INCREMENT)")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("사용자 이메일 (로그인 ID, 유니크)")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Comment("BCrypt 암호화된 비밀번호 (소셜 로그인 시 null)")
    @Column(length = 100)
    private String password;

    @Comment("사용자 표시 이름")
    @Column(length = 50)
    private String displayName;

    @Comment("프로필 이미지 URL")
    @Column(length = 500)
    private String profileImageUrl;

    @Comment("사용자 권한 (ROLE_USER, ROLE_ADMIN)")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Comment("가입 경로 (LOCAL, GOOGLE)")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Provider provider;

    @Comment("소셜 로그인 고유 ID (Google sub 값)")
    @Column(length = 100)
    private String providerId;

    @Comment("이메일 인증 여부 (0=미인증, 1=인증)")
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private boolean emailVerified;

    @Comment("2차 인증 유형 (NONE, EMAIL, GOOGLE_OTP)")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TwoFactorType twoFactorType;

    @Comment("2FA 설정 이후 1회 이상 인증 성공 여부")
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private boolean twoFactorVerified;

    @Comment("Google OTP 시크릿 키")
    @Column(length = 64)
    private String twoFactorSecret;

    @Comment("이메일 2FA 인증 코드 (6자리)")
    @Column(length = 10)
    private String twoFactorCode;

    @Comment("이메일 2FA 인증 코드 만료 일시")
    private LocalDateTime twoFactorCodeExpiresAt;

    @Comment("2FA 인증 대기 만료 일시 (우회 방지)")
    private LocalDateTime twoFactorPendingUntil;

    @Comment("로그인 실패 횟수")
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private int failedLoginAttempts;

    @Comment("계정 잠금 해제 일시 (null=잠금 아님)")
    private LocalDateTime lockedUntil;

    @Comment("계정 정지 여부 (0=정상, 1=정지)")
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private boolean suspended;

    @Comment("계정 정지 사유")
    @Column(length = 500)
    private String suspendedReason;

    @Comment("계정 정지 해제 일시")
    private LocalDateTime suspendedUntil;

    @Comment("관리자 메모")
    @Column(length = 1000)
    private String adminNote;

    @Comment("소프트 삭제 일시 (null=활성)")
    private LocalDateTime deletedAt;

    @Comment("계정 생성 일시")
    @CreatedDate
    private LocalDateTime createdAt;

    @Comment("계정 최종 수정 일시")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ── 인증 관련 비즈니스 메서드 ──────────────────────────────────────────

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now());
    }

    public void generateTwoFactorCode(String code) {
        this.twoFactorCode = code;
        this.twoFactorCodeExpiresAt = LocalDateTime.now().plusMinutes(10);
    }

    public void startTwoFactor() {
        this.twoFactorPendingUntil = LocalDateTime.now().plusMinutes(10);
    }

    public boolean verifyTwoFactorPending() {
        return this.twoFactorPendingUntil != null
                && this.twoFactorPendingUntil.isAfter(LocalDateTime.now());
    }

    public boolean verifyTwoFactorCode(String code) {
        return this.twoFactorCode != null
                && this.twoFactorCode.equals(code)
                && this.twoFactorCodeExpiresAt != null
                && this.twoFactorCodeExpiresAt.isAfter(LocalDateTime.now());
    }

    public void clearTwoFactorPending() {
        this.twoFactorPendingUntil = null;
        this.twoFactorCode = null;
        this.twoFactorCodeExpiresAt = null;
    }

    public void updateTwoFactor(TwoFactorType type, String secret) {
        this.twoFactorType = type;
        this.twoFactorSecret = secret;
        this.twoFactorVerified = false;
    }

    public void verifyTwoFactor() {
        this.twoFactorVerified = true;
    }

    // ── 프로필 비즈니스 메서드 ────────────────────────────────────────────

    public void updateProfile(String displayName, String profileImageUrl) {
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
    }

    // ── 관리자 비즈니스 메서드 ────────────────────────────────────────────

    public void suspend(String reason, LocalDateTime until) {
        this.suspended = true;
        this.suspendedReason = reason;
        this.suspendedUntil = until;
    }

    public void unsuspend() {
        this.suspended = false;
        this.suspendedReason = null;
        this.suspendedUntil = null;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateAdminNote(String note) {
        this.adminNote = note;
    }
}
