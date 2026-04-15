package com.companyapp.backend.entity;

import com.companyapp.backend.enums.AccessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Přidáno pro lepší kompatibilitu s Builderem
@Builder // Přidáno pro snadnější vytváření testovacích dat
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Email(message = "E-mail musí být v platném formátu.")
    @NotBlank(message = "E-mail nesmí být prázdný.")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "PIN kód pro terminál je vyžadován.")
    @Column(name = "pin", nullable = false)
    private String pin;

    /**
     * Heslo pro webové rozhraní.
     * Poznámka k chybě 'Cannot resolve column':
     * Pokud IntelliJ svítí červeně, ujisti se, že v databázi v tabulce 'users'
     * tento sloupec skutečně existuje.
     */
    @Column(name = "password")
    private String password;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<AccessLevel> roles = new HashSet<>();

    /**
     * Seznam stanovišť, na která je uživatel kvalifikován.
     * DŮLEŽITÉ: Lombok díky @Getter vygeneruje metodu getQualifiedStations(),
     * kterou vyžaduje tvůj PositionSettingsServiceImpl.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_qualified_stations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    private Set<Station> qualifiedStations = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", optional = true, fetch = FetchType.LAZY)
    private PasswordResetToken passwordResetToken;

    // --- HELPER METODY ---

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public String getLastName() {
        return userProfile != null ? userProfile.getLastName() : null;
    }

    public String getFirstName() {
        return userProfile != null ? userProfile.getFirstName() : null;
    }

    // --- JPA SPECIFICKÉ EQUALS/HASHCODE ---
    // Používáme pouze ID, aby nedocházelo k cyklení s kolekcemi (StackOverflow)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}