package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    // Změněno z AndDateAnd na AndAvailableDateAnd
    boolean existsByUserIdAndAvailableDateAndStatus(UUID userId, LocalDate availableDate, AvailabilityStatus status);

    @Modifying
    @Query("UPDATE Availability a SET a.status = :status WHERE a.user.id = :userId AND a.availableDate = :availableDate")
    void updateStatusByUserIdAndAvailableDate(UUID userId, LocalDate availableDate, AvailabilityStatus status);

    boolean existsByUserIdAndDateAndStatus(UUID userId, LocalDate localDate);

    void updateStatusByUserIdAndDate(UUID userId, LocalDate localDate);
}