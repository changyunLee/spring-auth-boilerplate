package com.example.auth.service;

import com.example.auth.domain.User;
import com.example.auth.domain.TwoFactorType;
import com.example.auth.dto.UserDto;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public void updateProfile(String email, UserDto.ProfileUpdateRequest request) {
        User user = findByEmail(email);
        user.setDisplayName(request.getDisplayName());
        user.setProfileImageUrl(request.getProfileImageUrl());
    }

    @Transactional
    public void changePassword(String email, UserDto.PasswordChangeRequest request) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void updateTwoFactorType(String email, TwoFactorType type, String secret) {
        User user = findByEmail(email);
        user.setTwoFactorType(type);
        if (secret != null) {
            user.setTwoFactorSecret(secret);
        }
    }
}
