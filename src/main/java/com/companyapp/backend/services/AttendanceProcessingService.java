package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.AttendanceLogDto;
import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface AttendanceProcessingService {
    AttendanceLogDto clockIn(UUID userId, UUID shiftAssignmentId, Instant clockInTime);
    AttendanceLogDto clockOut(UUID userId, Instant clockOutTime);
    void rejectOvertime(UUID attendanceLogId, String reason);
    List<ShiftAssignmentDto> splitShiftAssignment(UUID shiftAssignmentId, ZonedDateTime breakStart, ZonedDateTime breakEnd);
}