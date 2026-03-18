package com.companyapp.backend.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "id")
    private UUID id;

    /**
     * Oboustranná vazba na User, která umožňuje získat profil pro daného uživatele. Na straně User je definována jako @OneToOne(mappedBy = "profile"). Tato vazba je nastavena jako @MapsId, což znamená, že primární klíč UserProfile bude stejný jako primární klíč User. To zajišťuje, že každý uživatel může mít pouze jeden profil a každý profil je spojen s jedním uživatelem. FetchType.LAZY znamená, že profil bude načten z databáze pouze tehdy, když bude explicitně požadován, což může zlepšit výkon při načítání uživatelů bez potřeby načítat jejich profily.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Základní informace o uživateli, které jsou důležité pro identifikaci a komunikaci s uživatelem. Tyto atributy jsou klíčové pro zobrazení informací o uživateli v aplikaci a pro personalizaci uživatelského zážitku. FirstName a LastName jsou povinné (NotBlank) a jsou uloženy jako nenulové sloupce v databázi, aby se zajistilo, že každý profil bude mít tyto základní informace.

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile that = (UserProfile) o;
        return id!= null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
