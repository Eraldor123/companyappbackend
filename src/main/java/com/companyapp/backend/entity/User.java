package com.companyapp.backend.entity;

import com.companyapp.backend.enums.AccessLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
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

    @Column(name = "password")
    private String password;

    /**
     * FÁZE 2: Změněno z EAGER na LAZY.
     * Role uživatele se nyní načtou pouze tehdy, když jsou skutečně potřeba.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<AccessLevel> roles = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_qualified_stations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    private Set<Station> qualifiedStations = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * FÁZE 2: Změněno na LAZY.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    /**
     * FÁZE 2: Změněno na LAZY.
     */
    @OneToOne(mappedBy = "user", optional = true, fetch = FetchType.LAZY)
    private PasswordResetToken passwordResetToken;

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

    public void setActive(boolean b) {
        this.isActive = b;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    // Metody getPinHash() a getPasswordHash() byly odstraněny,
    // protože jen mátly a využívaly starý HashUtil.
    // Pro získání hashe nyní stačí standardní getPin() a getPassword().

    public String getLastName() {
        return userProfile != null ? userProfile.getLastName() : null;
    }

    public String getFirstName() {
        return userProfile != null ? userProfile.getFirstName() : null;
    }
}