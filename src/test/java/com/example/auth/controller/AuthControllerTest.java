package com.example.auth.controller;

import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import com.example.auth.dto.AuthDto;
import com.example.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 컨트롤러 연동 테스트")
    void testSignupEndpoint() throws Exception {
        AuthDto.SignUpRequest request = new AuthDto.SignUpRequest();
        request.setEmail("signup@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입이 완료되었습니다. 이메일 인증 후 로그인하세요."));
    }

    @Test
    @DisplayName("로그인 컨트롤러 연동 테스트 (Refresh Token 반환 포함)")
    void testLoginEndpoint() throws Exception {
        // 미리 유저 생성
        User user = User.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("login@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
