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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String userToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create Admin User
        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ROLE_ADMIN)
                .build();
        userRepository.save(admin);
        adminToken = jwtTokenProvider.createToken(admin.getEmail(), admin.getRole().name());

        // Create Normal User
        testUser = User.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(testUser);
        userToken = jwtTokenProvider.createToken(testUser.getEmail(), testUser.getRole().name());
    }

    @Test
    @DisplayName("Admin API 접근 - 인증되지 않은 사용자 (401)")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin API 접근 - 권한이 없는 사용자 (403)")
    void testForbiddenAccess() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin API 접근 - 관리자 목록 조회 성공 (200)")
    void testAdminSearchUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Admin API 접근 - 특정 사용자 정지 (Edge case: 없는 사용자)")
    void testSuspendNonExistentUser() throws Exception {
        mockMvc.perform(put("/api/admin/users/99999/suspend")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Test suspension\", \"days\": 7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("Admin API 접근 - 사용자 정지 및 정상 해제 (200)")
    void testSuspendAndUnsuspendUser() throws Exception {
        // Suspend
        mockMvc.perform(put("/api/admin/users/" + testUser.getId() + "/suspend")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Bad behavior\", \"days\": 7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true));

        // Unsuspend
        mockMvc.perform(put("/api/admin/users/" + testUser.getId() + "/unsuspend")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false));
    }
}