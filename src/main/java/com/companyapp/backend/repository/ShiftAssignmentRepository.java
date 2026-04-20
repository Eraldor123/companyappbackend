package com.companyapp.backend.repository;

import com.companyapp.backend.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {

    /**
     * OPRAVA java:S1144: Metoda označena SuppressWarnings.
     * Slouží k detekci kolizí (překryvů), aby mohl frontend vizuálně odlišit
     * zaměstnance pracující na více stanovištích zároveň.
     */
    @SuppressWarnings("unused")
    @Query("SELECT COUNT(s) FROM ShiftAssignment s " +
            "WHERE s.employee.id = :employeeId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    long countOverlappingShifts(
            @Param("employeeId") UUID employeeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    long countByShiftId(UUID shiftId);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee.id = :userId " +
            "AND sa.startTime <= :windowEnd AND sa.endTime >= :windowStart")
    List<ShiftAssignment> findCurrentAssignments(
            @Param("userId") UUID userId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.shift.shiftDate BETWEEN :start AND :end")
    List<ShiftAssignment> findByShiftDateBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee.id IN :userIds AND sa.shift.shiftDate BETWEEN :start AND :end")
    List<ShiftAssignment> findAssignmentsForUsersInDateRange(
            @Param("userIds") List<UUID> userIds,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    void deleteByShiftIdAndEmployeeId(UUID shiftId, UUID userId);
    boolean existsByShiftIdAndEmployeeId(UUID shiftId, UUID employeeId);
}