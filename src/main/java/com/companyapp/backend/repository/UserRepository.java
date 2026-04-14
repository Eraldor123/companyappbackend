package com.companyapp.backend.repository;

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

    Optional<User> findByEmail(String email);

    /**
     * FÁZE 2: Oprava LazyInitializationException při loginu.
     * EntityGraph zajistí, že se role načtou dychtivě (Eager) spolu s uživatelem,
     * což zabrání chybě 'no Session' při ověřování oprávnění v Security.
     */
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByPinAndIsActiveTrue(String pin);

    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") UUID userId);

    boolean existsByEmail(@Email(message = "E-mail nemá správný formát.") @NotBlank(message = "E-mail je povinný.") String email);

    /**
     * FÁZE 2: Implementace stránkování a mitigace N+1 selectu.
     * EntityGraph zajistí, že se profil a kvalifikace načtou v jednom SQL dotazu (JOIN).
     * Pageable umožní frontendu žádat o data po částech, což šetří RAM serveru.
     */
    @EntityGraph(attributePaths = {"userProfile", "qualifiedStations"})
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActiveUsersWithDetails(Pageable pageable);

    // NAJDE VŠECHNY LIDI, KTEŘÍ MAJÍ DANOU KVALIFIKACI
    List<User> findAllByQualifiedStationsContains(com.companyapp.backend.entity.Station station);
}