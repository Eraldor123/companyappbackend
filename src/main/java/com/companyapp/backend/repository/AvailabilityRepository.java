package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {

    Optional<Availability> findByUserIdAndAvailableDate(UUID userId, LocalDate availableDate);

    List<Availability> findByAvailableDateBetween(LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT * FROM availabilities a WHERE a.status = CAST(:status AS availability_status) AND a.available_date = :date", nativeQuery = true)
    List<Availability> findByDateAndStatusNative(
            @Param("date") LocalDate date,
            @Param("status") String status);
}