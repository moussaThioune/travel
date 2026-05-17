package com.travelagency.controller;

import com.travelagency.dto.AuthDTOs;
import com.travelagency.service.AuthService;
import com.travelagency.service.EmailService;
import com.travelagency.entity.User;
import com.travelagency.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        User user = currentUser();
        return ResponseEntity.ok(ProfileResponse.from(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        User user = currentUser();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user = userRepository.save(user);
        return ResponseEntity.ok(ProfileResponse.from(user));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        User user = currentUser();
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mot de passe actuel incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Mot de passe modifié avec succès");
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    @Data
    public static class ProfileResponse {
        private String firstName;
        private String lastName;
        private String email;
        private String role;

        public static ProfileResponse from(User u) {
            ProfileResponse r = new ProfileResponse();
            r.firstName = u.getFirstName();
            r.lastName = u.getLastName();
            r.email = u.getEmail();
            r.role = u.getRole().name();
            return r;
        }
    }

    @Data
    public static class UpdateProfileRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank private String newPassword;
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
