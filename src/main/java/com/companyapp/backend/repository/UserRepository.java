package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Station;
import com.companyapp.backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Nezbytná pro backendové validace a administrativní správu uživatelů napříč stavy.
     */
    @SuppressWarnings("unused")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByPinAndIsActiveTrue(String pin);

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Realizuje bezpečné "smazání" (deaktivaci) uživatele bez ztráty historických dat.
     */
    @SuppressWarnings("unused")
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") UUID userId);

    boolean existsByEmail(@Email @NotBlank String email);

    @EntityGraph(attributePaths = {"userProfile", "qualifiedStations"})
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActiveUsersWithDetails(Pageable pageable);

    /**
     * Najde všechny lidi, kteří mají danou kvalifikaci.
     * OPRAVA: Odstraněna plná cesta k balíčku Station pro lepší čitelnost.
     */
    List<User> findAllByQualifiedStationsContains(Station station);
}