package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    // ==========================================================
    // 1. NOVÉ METODY PRO REACT KALENDÁŘ
    // ==========================================================
    @Query("SELECT a FROM Availability a WHERE a.userId = :userId AND a.availableDate BETWEEN :startDate AND :endDate")
    List<Availability> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings, protože je připravena
     * pro budoucí integraci s frontendovým kalendářem (hromadné mazání).
     */
    @SuppressWarnings("unused")
    @Modifying
    @Query("DELETE FROM Availability a WHERE a.userId = :userId AND a.availableDate BETWEEN :startDate AND :endDate")
    void deleteByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // ==========================================================
    // 2. STARÉ METODY (Zpětná kompatibilita pro ShiftAssignment)
    // ==========================================================

    @Query("SELECT COUNT(a) > 0 FROM Availability a WHERE a.userId = :userId AND a.availableDate = :date")
    boolean existsByUserIdAndAvailableDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );

    @Modifying
    @Query("UPDATE Availability a SET a.confirmed = true WHERE a.userId = :userId AND a.availableDate = :date")
    void updateStatusByUserIdAndAvailableDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );

    @Query("SELECT a FROM Availability a WHERE a.availableDate BETWEEN :startDate AND :endDate")
    List<Availability> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}