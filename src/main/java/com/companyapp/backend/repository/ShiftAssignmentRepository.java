package com.companyapp.backend.repository;

import com.companyapp.backend.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {

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
    java.util.List<ShiftAssignment> findCurrentAssignments(
            @Param("userId") UUID userId,
            @Param("windowStart") java.time.LocalDateTime windowStart,
            @Param("windowEnd") java.time.LocalDateTime windowEnd
    );

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.shift.shiftDate BETWEEN :start AND :end")
    java.util.List<ShiftAssignment> findByShiftDateBetween(
            @Param("start") java.time.LocalDate start,
            @Param("end") java.time.LocalDate end
    );

    // Rychle najde všechny směny zadaných lidí v daném měsíci (pro výpočet statistik)
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee.id IN :userIds AND sa.shift.shiftDate BETWEEN :start AND :end")
    java.util.List<ShiftAssignment> findAssignmentsForUsersInDateRange(
            @Param("userIds") java.util.List<UUID> userIds,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    void deleteByShiftIdAndEmployeeId(UUID shiftId, UUID userId);
}