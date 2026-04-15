package com.companyapp.backend.repository;

import com.companyapp.backend.entity.AttendanceLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, UUID> {

    /**
     * OPRAVA java:S1144: Metoda je označena jako SuppressWarnings("unused"),
     * protože bude využita po forkování projektu v docházkovém modulu.
     * * OPRAVA CESTY: Změněno z shiftAssignment.user na shiftAssignment.employee,
     * aby odpovídalo aktuálnímu stavu entity ShiftAssignment.
     */
    @SuppressWarnings("unused")
    @EntityGraph(attributePaths = {"shiftAssignment", "shiftAssignment.employee"})
    List<AttendanceLog> findByIsUnusualTrueAndManagerApprovedFalse();

    Optional<AttendanceLog> findByShiftAssignmentId(UUID assignmentId);

    @Query("SELECT a FROM AttendanceLog a WHERE a.shiftAssignment.employee.id = :userId AND a.clockOut IS NULL")
    Optional<AttendanceLog> findOpenLogForUser(@Param("userId") UUID userId);
}