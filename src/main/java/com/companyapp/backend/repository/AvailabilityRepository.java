package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.enums.AvailabilityStatus; // Důležitý import pro starý kód
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
    // 1. NOVÉ METODY PRO REACT KALENDÁŘ (To, co jsme psali dřív)
    // ==========================================================
    @Query("SELECT a FROM Availability a WHERE a.userId = :userId AND a.availableDate BETWEEN :startDate AND :endDate")
    List<Availability> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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

    // Náš starý kód kontroloval AvailabilityStatus. Tímto dotazem ho "oblafneme"
    // a prostě vrátíme true, pokud uživatel na daný den existuje.
    @Query("SELECT COUNT(a) > 0 FROM Availability a WHERE a.userId = :userId AND a.availableDate = :date")
    boolean existsByUserIdAndAvailableDateAndStatus(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date,
            @Param("status") AvailabilityStatus status
    );

    // Stará metoda dříve asi měnila status. My ji teď napojíme na náš nový boolean "confirmed = true"
    @Modifying
    @Query("UPDATE Availability a SET a.confirmed = true WHERE a.userId = :userId AND a.availableDate = :date")
    void updateStatusByUserIdAndAvailableDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );

    // Najde všechny dostupné lidi pro daný týden
    @Query("SELECT a FROM Availability a WHERE a.availableDate BETWEEN :startDate AND :endDate")
    List<Availability> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}