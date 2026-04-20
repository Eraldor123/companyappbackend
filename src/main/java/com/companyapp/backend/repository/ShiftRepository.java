// src/main/java/com/companyapp/backend/repository/ShiftRepository.java

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
     * Vyhledávání směn pro konkrétní stanoviště.
     * EntityGraph zajišťuje, že se rovnou načtou i přiřazení zaměstnanci.
     */
    @EntityGraph(attributePaths = {"station", "template", "assignments", "assignments.employee"})
    List<Shift> findByShiftDateBetweenAndStationId(LocalDate startDate, LocalDate endDate, Integer stationId);

    /**
     * Pomocná metoda pro validaci – hledá směny, které se časově překrývají na stejném stanovišti.
     */
    @Query("SELECT s FROM Shift s WHERE s.startTime < :endTime AND s.endTime > :startTime AND s.station.id = :stationId")
    List<Shift> findOverlappingShifts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("stationId") Integer stationId);

    /**
     * KLÍČOVÁ OPRAVA (N+1 PROBLÉM):
     * Načte všechny směny v týdnu pro hlavní mřížku Směnáře.
     * attributePaths obsahuje "assignments.employee", což Hibernate donutí vytáhnout
     * jména brigádníků hned v prvním dotazu pomocí JOINu.
     */
    @EntityGraph(attributePaths = {"station", "template", "assignments", "assignments.employee"})
    List<Shift> findByShiftDateBetween(LocalDate start, LocalDate end);

    /**
     * PESIMISTICKÉ ZAMYKÁNÍ (Fáze 1 / Bod 3):
     * Uzamkne řádek v databázi pro zápis (FOR UPDATE).
     * Ostatní manažeři musí počkat, než se dokončí tato transakce, čímž se předejde
     * přepsání kapacity směny při souběžném kliknutí.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT s FROM Shift s WHERE s.id = :id")
    Optional<Shift> findByIdWithLock(@Param("id") UUID id);
}