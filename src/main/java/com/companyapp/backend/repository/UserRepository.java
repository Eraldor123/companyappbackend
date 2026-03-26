package com.companyapp.backend.repository;

import com.companyapp.backend.entity.User;
import com.companyapp.backend.enums.AccessLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByPinAndIsActiveTrue(String pin);

    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") UUID userId);

    boolean existsByEmail(@Email(message = "E-mail nemá správný formát.") @NotBlank(message = "E-mail je povinný.") String email);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"userProfile", "qualifiedStations"})
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsersWithDetails();
}