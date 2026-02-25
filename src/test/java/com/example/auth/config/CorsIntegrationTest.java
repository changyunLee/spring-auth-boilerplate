package com.example.auth.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("CORS preflight - OPTIONS 요청 200 응답 + CORS 헤더 반환")
    void corsPreflightReturns200WithCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    @DisplayName("CORS - 허용된 Origin의 실제 요청에 Access-Control-Allow-Origin 헤더 포함")
    void actualRequestFromAllowedOriginHasCorsHeader() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"cors@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    @DisplayName("CORS - 허용되지 않은 Origin은 Access-Control-Allow-Origin 헤더 없음")
    void requestFromUnallowedOriginHasNoCorsHeader() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .header("Origin", "https://evil.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"cors@example.com\",\"password\":\"Password123!\"}"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
