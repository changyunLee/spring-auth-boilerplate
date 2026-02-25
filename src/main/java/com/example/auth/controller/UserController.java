package com.example.auth.controller;

import com.example.auth.domain.User;
import com.example.auth.dto.UserDto;
import com.example.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.findByEmail(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto.ProfileUpdateRequest request) {
        userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto.PasswordChangeRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/2fa")
    public ResponseEntity<Object> change2fa(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto.TwoFactorChangeRequest request) {
        // Simple mock for recovery - secret generation logic omitted for brevity but can be added back
        userService.updateTwoFactorType(userDetails.getUsername(), request.getTwoFactorType(), null);
        return ResponseEntity.ok().build();
    }
}
