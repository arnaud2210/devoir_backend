package edu.arnaud.devoir_backend.authentication.controller;

import com.fasterxml.jackson.annotation.JsonView;
import edu.arnaud.devoir_backend.authentication.dto.ApiResponse;
import edu.arnaud.devoir_backend.authentication.dto.ForgotPasswordRequest;
import edu.arnaud.devoir_backend.authentication.dto.RegisterRequest;
import edu.arnaud.devoir_backend.authentication.dto.ResetPasswordRequest;
import edu.arnaud.devoir_backend.authentication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @JsonView(Views.Public.class)
    public ResponseEntity<ApiResponse> register (@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse(true,"Vérifiez votre email pour activer votre compte."));
    }

    @GetMapping("/verify")
    @JsonView(Views.Public.class)
    public ResponseEntity<ApiResponse> verify(@RequestParam String token) {
        authService.verifyAccount(token);
        return ResponseEntity.ok(new ApiResponse(true,"Votre compte est activé"));
    }

    @PostMapping("/forgot-password")
    @JsonView(Views.Public.class)
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new ApiResponse(true,"Email de réinitialisation envoyé"));
    }

    @PostMapping("/reset-password")
    @JsonView(Views.Public.class)
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam @Valid ResetPasswordRequest request) {
        authService.resetPassword(token, request.getNewPassword());
        return ResponseEntity.ok(new ApiResponse(true,"Mot de passe mis à jour"));
    }
}
