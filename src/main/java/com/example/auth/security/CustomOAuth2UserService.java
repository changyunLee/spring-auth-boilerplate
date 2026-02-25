package com.example.auth.security;

import com.example.auth.domain.Provider;
import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements
        org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    /**
     * Google의 userinfo 엔드포인트를 호출합니다.
     * 테스트에서 @Spy로 이 메서드를 모킹하여 실제 HTTP 호출을 대체합니다.
     */
    protected OAuth2User loadGoogleUser(OAuth2UserRequest userRequest) {
        return new DefaultOAuth2UserService().loadUser(userRequest);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User googleUser = loadGoogleUser(userRequest);

        String email = googleUser.getAttribute("email");
        String providerId = googleUser.getName();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewGoogleUser(email, providerId, googleUser));

        java.util.Map<String, Object> attributes = new java.util.HashMap<>(googleUser.getAttributes());
        attributes.put("email", user.getEmail());

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                "email"
        );
    }

    private User registerNewGoogleUser(String email, String providerId, OAuth2User googleUser) {
        return userRepository.save(User.builder()
                .email(email)
                .password("")
                .role(Role.ROLE_USER)
                .provider(Provider.GOOGLE)
                .providerId(providerId)
                .emailVerified(true)
                .build());
    }
}
