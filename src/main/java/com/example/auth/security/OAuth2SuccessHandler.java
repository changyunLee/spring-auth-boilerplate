package com.example.auth.security;

import com.example.auth.dto.AuthDto;
import com.example.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        AuthDto.TokenResponse tokens = authService.generateOAuth2Tokens(email, role);

        // fragment(#)를 사용하여 토큰이 서버 로그 및 Referer 헤더에 기록되지 않도록 함
        String redirectUrl = frontendUrl + "/dashboard.html"
                + "#accessToken=" + tokens.getAccessToken()
                + "&refreshToken=" + tokens.getRefreshToken();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
