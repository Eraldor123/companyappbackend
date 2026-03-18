package com.companyapp.backend.repository;

import com.companyapp.backend.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}