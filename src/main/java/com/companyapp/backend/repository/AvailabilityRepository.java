package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    boolean existsByUserIdAndAvailableDateAndStatus(UUID userId, LocalDate availableDate, AvailabilityStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Availability a SET a.status = :status WHERE a.user.id = :userId AND a.availableDate = :availableDate")
    void updateStatusByUserIdAndAvailableDate(
            @Param("userId") UUID userId,
            @Param("availableDate") LocalDate availableDate,
            @Param("status") AvailabilityStatus status
    );

}