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
@AllArgsConstructor
@Builder
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
     * Poznámka: Pokud IntelliJ stále hlásí 'Cannot resolve column',
     * zkus Refresh v záložce Database (Alt+1 -> Database).
     */
    @Column(name = "password")
    private String password;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Set<AccessLevel> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_qualified_stations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    @Builder.Default
    private Set<Station> qualifiedStations = new HashSet<>();

    // OPRAVA: Přidáno @Builder.Default, aby builder neignoroval true
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * OPRAVA: Odstraněno fetch=LAZY a optional=true.
     * Na non-owning side OneToOne je LAZY ignorováno a optional=true je default.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile userProfile;

    /**
     * OPRAVA: Odstraněno fetch=LAZY a optional=true.
     */
    @OneToOne(mappedBy = "user")
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

    // --- STANDARDNÍ METODY ---

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