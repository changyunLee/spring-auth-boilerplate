package com.example.auth.service;

import com.example.auth.domain.Role;
import com.example.auth.domain.User;
import com.example.auth.domain.TwoFactorType;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void suspendUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Prevent suspending the last admin if needed (logic can be added)
        user.setSuspended(true);
        user.setSuspendedReason(reason);
    }

    @Transactional
    public void unsuspendUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setSuspended(false);
        user.setSuspendedReason(null);
    }

    @Transactional
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLocked(false);
        user.setLoginAttempts(0);
        user.setLockTime(null);
    }

    @Transactional
    public void changeUserRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
    }

    @Transactional
    public void resetUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void changeUser2fa(Long id, TwoFactorType type) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorType(type);
    }
}
