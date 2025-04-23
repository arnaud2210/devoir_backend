package edu.arnaud.devoir_backend.authentication;

import edu.arnaud.devoir_backend.authentication.dto.RegisterRequest;
import edu.arnaud.devoir_backend.authentication.entity.User;
import edu.arnaud.devoir_backend.authentication.repository.UserRepository;
import edu.arnaud.devoir_backend.authentication.repository.VerificationTokenRepository;
import edu.arnaud.devoir_backend.authentication.service.AuthService;
import edu.arnaud.devoir_backend.authentication.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "Password123");
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashedPassword");

        // Act
        authService.register(request);

        // Assert
        verify(userRepository, times(1)).save(any());  // Vérifie que l'utilisateur est sauvegardé
        // verify(mailService, times(1)).sendSimpleMail(any());  // Vérifie que l'email de vérification est envoyé
    }

    @Test
    void shouldThrowEmailAlreadyExistsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "Password123");
        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);

        // Act & Assert
        assertThrows(MailAuthenticationException.class, () -> authService.register(request));
    }
}
