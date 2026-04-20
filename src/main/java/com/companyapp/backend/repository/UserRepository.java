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

    @SuppressWarnings("unused")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByPinAndIsActiveTrue(String pin);

    @SuppressWarnings("unused")
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") UUID userId);

    boolean existsByEmail(@Email @NotBlank String email);

    @EntityGraph(attributePaths = {"userProfile", "qualifiedStations"})
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Page<User> findAllActiveUsersWithDetails(Pageable pageable);

    /**
     * OPRAVA BYTEA CHYBY: Podmínka zkontrolována na prázdný string ('') místo IS NULL.
     */
    @EntityGraph(attributePaths = {"userProfile", "qualifiedStations"})
    @Query("SELECT u FROM User u " +
            "LEFT JOIN u.userProfile up " +
            "WHERE u.isActive = true " +
            "AND (:search = '' OR LOWER(up.firstName) LIKE CONCAT('%', :search, '%') OR LOWER(up.lastName) LIKE CONCAT('%', :search, '%')) " +
            "AND (:contractType = '' OR EXISTS (SELECT 1 FROM Contract c WHERE c.user.id = u.id AND CAST(c.type AS string) = :contractType))")
    Page<User> findFilteredActiveUsersWithDetails(
            @Param("search") String search,
            @Param("contractType") String contractType,
            Pageable pageable
    );

    List<User> findAllByQualifiedStationsContains(Station station);
}