package edu.arnaud.devoir_backend.authentication.service;

import edu.arnaud.devoir_backend.authentication.dto.RegisterRequest;
import edu.arnaud.devoir_backend.authentication.entity.User;
import edu.arnaud.devoir_backend.authentication.entity.VerificationToken;
import edu.arnaud.devoir_backend.authentication.entity.VerificationType;
import edu.arnaud.devoir_backend.authentication.repository.UserRepository;
import edu.arnaud.devoir_backend.authentication.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email existe déjà");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setType(VerificationType.REGISTER);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        String url = "http://localhost:4200/verify?token=" + token;
        mailService.sendSimpleMail(
                user.getEmail(),
                "Vérification email",
                "<p>Merci pour votre inscription. Cliquez ici pour activer :</p><a href=\"" + url + "\">Activer</a>"
        );
    }

    public void verifyAccount(String token) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, VerificationType.REGISTER)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setType(VerificationType.RESET_PASSWORD);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        tokenRepository.save(verificationToken);

        String url = "http://localhost:4200/reset-password?token=" + token;
        mailService.sendSimpleMail(
                user.getEmail(),
                "Réinitialisation mot de passe",
                "<p>Cliquez pour réinitialiser : <a href=\"" + url + "\">Réinitialiser</a></p>"
        );
    }

    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndType(token, VerificationType.RESET_PASSWORD)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré");
        }

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
    }
}
