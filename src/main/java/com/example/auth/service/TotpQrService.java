package com.example.auth.service;

import com.example.auth.domain.Provider;
import com.example.auth.domain.Role;
import com.example.auth.domain.TwoFactorType;
import com.example.auth.domain.User;
import com.example.auth.dto.AdminDto;
import com.example.auth.dto.UserDto;
import com.example.auth.repository.UserRepository;
import com.example.auth.spec.UserSpecification;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final com.example.auth.repository.AuditLogRepository auditLogRepository;
    private final com.example.auth.repository.RefreshTokenRepository refreshTokenRepository;
    private final AuditLogService auditLogService;
    private final TotpQrService totpQrService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserDto.UserResponse> searchUsers(AdminDto.UserSearchRequest request, Pageable pageable) {
        Provider provider = null;
        if (request.getProvider() != null && !request.getProvider().isBlank()) {
            provider = Provider.valueOf(request.getProvider());
        }
        Specification<User> spec = Specification
                .where(UserSpecification.hasEmailContaining(request.getEmail()))
                .and(UserSpecification.hasRole(request.getRole()))
                .and(UserSpecification.hasProvider(provider))
                .and(UserSpecification.isEmailVerified(request.getEmailVerified()))
                .and(UserSpecification.isLocked(request.getIsLocked()));
        return userRepository.findAll(spec, pageable).map(UserDto.UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserDto.UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AdminDto.DashboardStats getDashboardStats() {
        return new AdminDto.DashboardStats(
                userRepository.count(),
                userRepository.countByRole(Role.ROLE_ADMIN),
                userRepository.countByRole(Role.ROLE_USER),
                userRepository.countByEmailVerified(true),
                userRepository.countByLockedUntilAfter(LocalDateTime.now()),
                userRepository.countByProvider(Provider.LOCAL),
                userRepository.countByProvider(Provider.GOOGLE),
                userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7))
        );
    }

    @Transactional(readOnly = true)
    public AdminDto.RegistrationTrendResponse getRegistrationTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> rawData = userRepository.countByCreatedAtGroupByDate(since);

        List<AdminDto.DailyCount> dailyCounts = rawData.stream()
                .map(row -> new AdminDto.DailyCount((LocalDate) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        return new AdminDto.RegistrationTrendResponse(days, dailyCounts);
    }

    @Transactional(readOnly = true)
    public AdminDto.LoginTrendResponse getLoginTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> successData = auditLogRepository.countByActionGroupByDate("LOGIN_SUCCESS", since);
        List<Object[]> failData = auditLogRepository.countByActionGroupByDate("LOGIN_FAIL", since);

        Map<LocalDate, Long> successMap = successData.stream()
                .collect(Collectors.toMap(row -> (LocalDate) row[0], row -> ((Number) row[1]).longValue()));
        Map<LocalDate, Long> failMap = failData.stream()
                .collect(Collectors.toMap(row -> (LocalDate) row[0], row -> ((Number) row[1]).longValue()));

        List<AdminDto.DailyLoginCount> data = new ArrayList<>();
        LocalDate current = LocalDate.now().minusDays(days - 1);
        for (int i = 0; i < days; i++) {
            long sCount = successMap.getOrDefault(current, 0L);
            long fCount = failMap.getOrDefault(current, 0L);
            data.add(new AdminDto.DailyLoginCount(current, sCount, fCount));
            current = current.plusDays(1);
        }

        return new AdminDto.LoginTrendResponse(days, data);
    }

    @Transactional(readOnly = true)
    public AdminDto.SecuritySummary getSecuritySummary() {
        long lockedAccounts = userRepository.countByLockedUntilAfter(LocalDateTime.now());
        long suspendedAccounts = userRepository.countBySuspendedTrue();

        LocalDateTime past24Hours = LocalDateTime.now().minusHours(24);
        long recentLoginFailures = auditLogRepository.countByActionAndCreatedAtAfter("LOGIN_FAIL", past24Hours);
        long suspiciousIps = auditLogRepository.countDistinctIpByActionSince("LOGIN_FAIL", past24Hours);

        return new AdminDto.SecuritySummary(lockedAccounts, suspendedAccounts, recentLoginFailures, suspiciousIps);
    }

    @Transactional
    public void changeUserRole(Long id, AdminDto.RoleChangeRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equals(currentEmail)) {
            throw new IllegalArgumentException("자기 자신의 권한을 강등하거나 변경할 수 없습니다.");
        }

        if (request.getRole() == Role.ROLE_USER && user.getRole() == Role.ROLE_ADMIN) {
            long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("시스템의 유일한 관리자는 권한을 잃을 수 없습니다.");
            }
        }

        user.changeRole(request.getRole());
        auditLogService.log("ROLE_CHANGE", user.getEmail(),
                "role=" + request.getRole().name() + ", changedBy=" + currentEmail);
    }

    @Transactional
    public AdminDto.TotpSetupResponse changeUserTwoFactor(Long id, AdminDto.TwoFactorChangeRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TwoFactorType type = TwoFactorType.valueOf(request.getTwoFactorType());

        if (type == TwoFactorType.GOOGLE_OTP) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            String secret = gAuth.createCredentials().getKey();
            String qrCodeBase64 = totpQrService.generateQrCodeBase64(user.getEmail(), secret);
            user.updateTwoFactor(type, secret);
            auditLogService.log("2FA_CHANGE", user.getEmail(), "type=" + type.name());
            return new AdminDto.TotpSetupResponse(secret, qrCodeBase64);
        }

        user.updateTwoFactor(type, null);
        auditLogService.log("2FA_CHANGE", user.getEmail(), "type=" + type.name());
        return null;
    }

    @Transactional
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.resetFailedLoginAttempts();
        auditLogService.log("ACCOUNT_UNLOCK", user.getEmail(), "unlocked by admin");
    }

    @Transactional
    public AdminDto.SuspendResponse suspendUser(Long id, AdminDto.SuspendRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equals(currentEmail)) {
            throw new IllegalArgumentException("자기 자신의 계정을 정지할 수 없습니다.");
        }

        user.suspend(request.getReason(), request.getSuspendedUntil());
        auditLogService.log("ACCOUNT_SUSPENDED", user.getEmail(), "reason=" + request.getReason());
        
        refreshTokenRepository.findByEmail(user.getEmail()).ifPresent(refreshTokenRepository::delete);
        
        return new AdminDto.SuspendResponse(user.getId(), user.getEmail(), user.isSuspended(), user.getSuspendedReason(), user.getSuspendedUntil());
    }

    @Transactional
    public AdminDto.SuspendResponse unsuspendUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                
        user.unsuspend();
        auditLogService.log("ACCOUNT_UNSUSPENDED", user.getEmail(), "unlocked by admin");
        
        return new AdminDto.SuspendResponse(user.getId(), user.getEmail(), user.isSuspended(), user.getSuspendedReason(), user.getSuspendedUntil());
    }

    @Transactional
    public void forceLogout(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                
        refreshTokenRepository.findByEmail(user.getEmail()).ifPresent(refreshTokenRepository::delete);
        auditLogService.log("FORCE_LOGOUT", user.getEmail(), "forced by admin");
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equals(currentEmail)) {
            throw new IllegalArgumentException("자기 자신의 계정을 삭제할 수 없습니다.");
        }
        
        user.softDelete();
        refreshTokenRepository.findByEmail(user.getEmail()).ifPresent(refreshTokenRepository::delete);
        auditLogService.log("USER_DELETED", user.getEmail(), "soft deleted by admin");
    }

    @Transactional
    public void updateAdminNote(Long id, AdminDto.AdminNoteRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                
        user.updateAdminNote(request.getNote());
    }

    @Transactional(readOnly = true)
    public Page<AdminDto.ActiveSessionResponse> getActiveSessions(Pageable pageable) {
        return refreshTokenRepository.findAll(pageable)
                .map(rt -> new AdminDto.ActiveSessionResponse(rt.getEmail(), rt.getCreatedAt(), rt.getExpiresAt()));
    }

    @Transactional
    public void revokeSession(String email) {
        refreshTokenRepository.findByEmail(email).ifPresent(refreshTokenRepository::delete);
        auditLogService.log("SESSION_REVOKED", email, "revoked by admin");
    }

    @Transactional(readOnly = true)
    public List<AdminDto.SuspiciousIpResponse> getSuspiciousIps(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Object[]> data = auditLogRepository.countByActionGroupByIp("LOGIN_FAIL", since);
        
        return data.stream()
                .map(row -> new AdminDto.SuspiciousIpResponse((String) row[0], ((Number) row[1]).longValue(), (LocalDateTime) row[2]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminDto.FailedLoginSummary getFailedLoginSummary(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        long totalFailures = auditLogRepository.countByActionAndCreatedAtAfter("LOGIN_FAIL", since);
        long distinctIpCount = auditLogRepository.countDistinctIpByActionSince("LOGIN_FAIL", since);
        long distinctEmailCount = auditLogRepository.countDistinctEmailByActionSince("LOGIN_FAIL", since);
        
        return new AdminDto.FailedLoginSummary(hours, totalFailures, distinctIpCount, distinctEmailCount);
    }

    @Transactional
    public void resetUserPassword(Long id, AdminDto.PasswordResetRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenRepository.findByEmail(user.getEmail()).ifPresent(refreshTokenRepository::delete);
        auditLogService.log("PASSWORD_RESET", user.getEmail(), "forced by admin");
    }
}
