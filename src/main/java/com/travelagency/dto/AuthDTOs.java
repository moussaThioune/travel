package com.travelagency.dto;

import com.travelagency.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDTOs {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank public String firstName;
        @NotBlank public String lastName;
        @Email @NotBlank public String email;
        @NotBlank @Size(min = 6) public String password;
        public String phone;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @Email @NotBlank public String email;
        @NotBlank public String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RegisterResponse {
        public String email;
        public String firstName;
        public String message;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        public String token;
        public String type = "Bearer";
        public Long userId;
        public String email;
        public String firstName;
        public String lastName;
        public User.Role role;
    }
}
