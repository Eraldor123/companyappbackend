package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Shift;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
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
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Nezbytné pro budoucí filtrování směn podle konkrétního stanoviště v UI.
     * EntityGraph zajišťuje optimální načtení přidružených dat v jednom dotazu.
     */
    @SuppressWarnings("unused")
    @EntityGraph(attributePaths = {"station", "template", "assignments"})
    List<Shift> findByShiftDateBetweenAndStationId(LocalDate startDate, LocalDate endDate, Integer stationId);

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Klíčové pro validaci integrity plánu – brání kolizi dvou směn na stejném stanovišti.
     */
    @SuppressWarnings("unused")
    @Query("SELECT s FROM Shift s WHERE s.startTime < :endTime AND s.endTime > :startTime AND s.station.id = :stationId")
    List<Shift> findOverlappingShifts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("stationId") Integer stationId);

    /**
     * FÁZE 2: Oprava N+1 pro hlavní výpis směn.
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