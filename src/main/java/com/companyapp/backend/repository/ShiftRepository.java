package com.companyapp.backend.repository;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    @EntityGraph(attributePaths = {"station", "template"})
    List<Shift> findByShiftDateBetweenAndStationId(LocalDate startDate, LocalDate endDate, Integer stationId);

    @Query("SELECT s FROM Shift s WHERE s.startTime < :endTime AND s.endTime > :startTime AND s.station.id = :stationId")
    List<Shift> findOverlappingShifts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("stationId") Integer stationId);
    List<Shift> findByShiftDateBetween(LocalDate start, LocalDate end);
}