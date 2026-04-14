package com.companyapp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    /**
     * FÁZE 2: Oprava N+1 problému.
     * Změněno z EAGER na LAZY. Uživatel se načte pouze v případě,
     * že skutečně dojde k potvrzení resetu hesla.
     */
    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Pomocná metoda pro kontrolu, jestli token už nevypršel
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}