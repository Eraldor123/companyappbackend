package com.companyapp.backend.repository;

import com.companyapp.backend.entity.AttendanceLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, UUID> {

    @EntityGraph(attributePaths = {"shiftAssignment", "shiftAssignment.user"})
    List<AttendanceLog> findByIsUnusualTrueAndManagerApprovedFalse();

    Optional<AttendanceLog> findByShiftAssignmentId(UUID assignmentId);
}