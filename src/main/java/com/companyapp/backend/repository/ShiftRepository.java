package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Shift;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    /**
     * FÁZE 2: Rozšířený EntityGraph.
     * Načítáme i 'assignments' (přiřazení), aby se při výpisu směn neprováděly
     * další dotazy na to, kdo na směně pracuje.
     */
    @EntityGraph(attributePaths = {"station", "template", "assignments"})
    List<Shift> findByShiftDateBetweenAndStationId(LocalDate startDate, LocalDate endDate, Integer stationId);

    @Query("SELECT s FROM Shift s WHERE s.startTime < :endTime AND s.endTime > :startTime AND s.station.id = :stationId")
    List<Shift> findOverlappingShifts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("stationId") Integer stationId);

    /**
     * FÁZE 2: Oprava N+1 pro hlavní výpis směn.
     * Přidán EntityGraph, který zajistí, že se stanice a šablony načtou v jednom dotazu (JOIN).
     */
    @EntityGraph(attributePaths = {"station", "template"})
    List<Shift> findByShiftDateBetween(LocalDate start, LocalDate end);

    /**
     * FÁZE 1: Pesimistické zamykání (PESSIMISTIC_WRITE).
     * SELECT ... FOR UPDATE s timeoutem 3000ms.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT s FROM Shift s WHERE s.id = :id")
    Optional<Shift> findByIdWithLock(@Param("id") UUID id);
}