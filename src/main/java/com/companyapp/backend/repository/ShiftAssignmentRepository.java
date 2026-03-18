package com.companyapp.backend.repository;

import com.companyapp.backend.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {

    @EntityGraph(attributePaths = {"user", "user.userProfile"})
    List<ShiftAssignment> findByShiftId(UUID shiftId);

    @Query("SELECT sa FROM ShiftAssignment sa JOIN FETCH sa.shift s " +
            "WHERE sa.user.id = :userId AND s.shiftDate BETWEEN :startDate AND :endDate")
    List<ShiftAssignment> findAssignmentsForUserInDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}