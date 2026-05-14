package com.travelagency.service;

import com.travelagency.dto.AuthDTOs;
import com.travelagency.entity.Client;
import com.travelagency.entity.User;
import com.travelagency.repository.ClientRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public AuthDTOs.RegisterResponse register(AuthDTOs.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email)) {
            throw new RuntimeException("Email déjà utilisé: " + request.email);
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .firstName(request.firstName)
                .lastName(request.lastName)
                .email(request.email)
                .password(passwordEncoder.encode(request.password))
                .role(User.Role.CLIENT)
                .enabled(false)
                .emailVerificationToken(verificationToken)
                .build();
        userRepository.save(user);

        Client client = Client.builder()
                .firstName(request.firstName)
                .lastName(request.lastName)
                .email(request.email)
                .phone(request.phone)
                .user(user)
                .build();
        clientRepository.save(client);

        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
        emailService.sendEmailVerification(user, verificationLink);

        return AuthDTOs.RegisterResponse.builder()
                .email(request.email)
                .firstName(request.firstName)
                .message("Un email de vérification a été envoyé à " + request.email + ". Veuillez cliquer sur le lien pour activer votre compte.")
                .build();
    }

    @Transactional
    public AuthDTOs.RegisterResponse resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé pour cet email."));
        if (user.isEnabled()) {
            throw new RuntimeException("Ce compte est déjà activé. Connectez-vous directement.");
        }
        String newToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(newToken);
        userRepository.save(user);

        String verificationLink = frontendUrl + "/verify-email?token=" + newToken;
        emailService.sendEmailVerification(user, verificationLink);

        return AuthDTOs.RegisterResponse.builder()
                .email(email)
                .firstName(user.getFirstName())
                .message("Email de vérification renvoyé à " + email)
                .build();
    }

    @Transactional
    public AuthDTOs.AuthResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Lien de vérification invalide ou expiré."));

        user.setEnabled(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return buildAuthResponse(user, jwtToken);
    }

    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email, request.password)
            );
        } catch (DisabledException e) {
            throw new RuntimeException("Compte non activé. Veuillez vérifier votre email pour activer votre compte.");
        }
        User user = userRepository.findByEmail(request.email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        String token = jwtService.generateToken(user);
        return buildAuthResponse(user, token);
    }

    private AuthDTOs.AuthResponse buildAuthResponse(User user, String token) {
        return AuthDTOs.AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
