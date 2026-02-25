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
    private final com.example.auth.service.TotpQrService totpQrService;

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
        
        String secret = null;
        java.util.Map<String, String> response = new java.util.HashMap<>();

        if (request.getTwoFactorType() == com.example.auth.domain.TwoFactorType.GOOGLE_OTP) {
            com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
            com.warrenstrange.googleauth.GoogleAuthenticatorKey key = gAuth.createCredentials();
            secret = key.getKey();
            
            String qrCodeBase64 = totpQrService.generateQrCodeBase64(userDetails.getUsername(), secret);
            response.put("secret", secret);
            response.put("qrCodeBase64", qrCodeBase64);
        }

        userService.updateTwoFactorType(userDetails.getUsername(), request.getTwoFactorType(), secret);
        
        return ResponseEntity.ok(response);
    }
}
