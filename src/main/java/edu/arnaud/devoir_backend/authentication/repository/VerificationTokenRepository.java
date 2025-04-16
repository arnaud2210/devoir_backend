package edu.arnaud.devoir_backend.authentication.repository;

import edu.arnaud.devoir_backend.authentication.entity.VerificationToken;
import edu.arnaud.devoir_backend.authentication.entity.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndType(String token, VerificationType type);
}
