package edu.arnaud.devoir_backend.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public class VerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    private VerificationType type;

    @ManyToOne
    private User user;
}
