package com.example.auth.controller;

import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("나의 정보 조회 - 인증된 사용자")
    void testGetCurrentUser() throws Exception {
        // given
        String email = "me@example.com";
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("1234"))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        String token = jwtTokenProvider.createToken(email, user.getRole().name());

        // when & then
        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("나의 정보 조회 - 인증되지 않은 사용자 (401/403)")
    void testGetCurrentUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized()); // OAuth2 추가 후 401 반환
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치 (400)")
    void testChangePasswordWithIncorrectCurrentPassword() throws Exception {
        // given
        String email = "pwd@example.com";
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("correct123!"))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        String token = jwtTokenProvider.createToken(email, user.getRole().name());

        String requestJson = "{" +
                "\"currentPassword\": \"wrong123!\"," +
                "\"newPassword\": \"newPassword123!\"" +
                "}";

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/user/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 일치하지 않습니다."));
    }
}
