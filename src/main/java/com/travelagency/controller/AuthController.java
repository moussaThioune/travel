package com.travelagency.controller;

import com.travelagency.dto.AuthDTOs;
import com.travelagency.service.AuthService;
import com.travelagency.service.EmailService;
import com.travelagency.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<AuthDTOs.RegisterResponse> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.AuthResponse> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<AuthDTOs.AuthResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthDTOs.RegisterResponse> resendVerification(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendVerification(email));
    }

    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        User fakeUser = User.builder()
            .firstName("Test").lastName("Admin").email(to).build();
        emailService.sendEmailVerification(fakeUser,
            "https://travelfront-production.up.railway.app/verify-email?token=TEST");
        return ResponseEntity.ok("Email de test envoyé à " + to + " — vérifiez les logs Railway.");
    }
}
