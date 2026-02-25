package com.example.auth.service;

import com.example.auth.domain.Role;
import com.example.auth.domain.TwoFactorType;
import com.example.auth.domain.User;
import com.example.auth.dto.AuthDto;
import com.example.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
public class AuthService2FATest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @Test
    public void testEmail2FALogin() {
        doNothing().when(emailService).send2FACodeEmail(any(), any());
        
        User user = User.builder()
                .email("test2fa@example.com")
                .password(passwordEncoder.encode("password123!"))
                .role(Role.ROLE_USER)
                .twoFactorType(TwoFactorType.EMAIL)
                .build();
        userRepository.save(user);

        AuthDto.LoginRequest loginReq = new AuthDto.LoginRequest();
        loginReq.setEmail("test2fa@example.com");
        loginReq.setPassword("password123!");
        
        AuthDto.TokenResponse tokenRes = authService.login(loginReq);
        assertThat(tokenRes.getStatus()).isEqualTo("REQUIRES_2FA");

        User savedUser = userRepository.findByEmail("test2fa@example.com").get();
        System.out.println("Pending until: " + savedUser.getTwoFactorPendingUntil());
        System.out.println("Code: " + savedUser.getTwoFactorCode());
        
        AuthDto.TwoFactorLoginRequest login2FAReq = new AuthDto.TwoFactorLoginRequest();
        login2FAReq.setEmail("test2fa@example.com");
        login2FAReq.setCode(savedUser.getTwoFactorCode());
        
        AuthDto.TokenResponse finalRes = authService.login2FA(login2FAReq);
        assertThat(finalRes.getAccessToken()).isNotNull();
    }
}