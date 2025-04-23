package edu.arnaud.devoir_backend.authentication;

import edu.arnaud.devoir_backend.authentication.dto.RegisterRequest;
import edu.arnaud.devoir_backend.authentication.entity.User;
import edu.arnaud.devoir_backend.authentication.entity.VerificationToken;
import edu.arnaud.devoir_backend.authentication.entity.VerificationType;
import edu.arnaud.devoir_backend.authentication.repository.UserRepository;
import edu.arnaud.devoir_backend.authentication.repository.VerificationTokenRepository;
import edu.arnaud.devoir_backend.authentication.service.AuthService;
import edu.arnaud.devoir_backend.authentication.service.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthTest {

    @Mock private UserRepository userRepository;
    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private MailService mailService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }

    @Test
    void testRegister_CreatesUserAndSendsEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@email.com");
        request.setPassword("Password1!");

        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setEmail("test@email.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(mailService).sendSimpleMail(
                eq("test@email.com"),
                eq("Vérification email"),
                contains("http://localhost:4200/verify?token=")
        );
    }

    @Test
    void testRegister_ThrowsExceptionIfEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password@123");
        lenient().when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void testVerifyAccount_ValidToken_SetsUserVerified() {
        User user = new User();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByTokenAndType("valid-token", VerificationType.REGISTER))
                .thenReturn(Optional.of(token));

        authService.verifyAccount("valid-token");

        assertTrue(user.isVerified());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void testVerifyAccount_InvalidToken_ThrowsException() {
        lenient().when(tokenRepository.findByTokenAndType("invalid", VerificationType.REGISTER))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.verifyAccount("invalid"));
    }

    @Test
    void testForgotPassword_SendsResetEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword("test@example.com");

        verify(tokenRepository).save(any(VerificationToken.class));
        verify(mailService).sendSimpleMail(
                eq("test@example.com"),
                eq("Réinitialisation mot de passe"),
                contains("http://localhost:4200/reset-password?token=")
        );
    }

    @Test
    void testForgotPassword_ThrowsIfUserNotFound() {
        lenient().when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.forgotPassword("notfound@example.com"));
    }

    @Test
    void testResetPassword_ValidToken_UpdatesPassword() {
        User user = new User();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        lenient().when(tokenRepository.findByTokenAndType("reset-token", VerificationType.RESET_PASSWORD))
                .thenReturn(Optional.of(token));
        lenient().when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPass");

        authService.resetPassword("reset-token", "newpass");

        assertEquals("encodedNewPass", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void testResetPassword_InvalidToken_ThrowsException() {
        lenient().when(tokenRepository.findByTokenAndType("invalid", VerificationType.RESET_PASSWORD))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword("invalid", "newpass"));
    }


}
