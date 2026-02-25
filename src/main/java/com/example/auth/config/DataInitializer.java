package com.example.auth.config;

import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-password:}")
    private String adminPassword;

    @Value("${app.admin-email:admin@example.com}")
    private String adminEmail;

    @Override
    public void run(String... args) throws Exception {
        if (!StringUtils.hasText(adminPassword)) {
            log.warn("[DataInitializer] ADMIN_INITIAL_PASSWORD 환경 변수가 설정되지 않아 초기 관리자 계정을 생성하지 않습니다.");
            return;
        }

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ROLE_ADMIN)
                    .emailVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("[DataInitializer] 초기 관리자 계정이 생성되었습니다: {}", adminEmail);
        }
    }
}
