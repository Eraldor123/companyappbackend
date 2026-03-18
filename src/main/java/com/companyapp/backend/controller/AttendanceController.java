package com.companyapp.backend.controller;

import com.companyapp.backend.services.AttendanceProcessingService;
import com.companyapp.backend.services.dto.response.AttendanceLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceProcessingService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceLogDto> clockIn(
            @RequestParam UUID userId,
            @RequestParam UUID shiftAssignmentId,
            @RequestParam Instant clockInTime) {
        AttendanceLogDto logDto = attendanceService.clockIn(userId, shiftAssignmentId, clockInTime);
        return ResponseEntity.ok(logDto);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceLogDto> clockOut(
            @RequestParam UUID userId,
            @RequestParam Instant clockOutTime) {
        AttendanceLogDto logDto = attendanceService.clockOut(userId, clockOutTime);
        return ResponseEntity.ok(logDto);
    }

    @PutMapping("/{attendanceLogId}/reject-overtime")
    public ResponseEntity<Void> rejectOvertime(
            @PathVariable UUID attendanceLogId,
            @RequestParam String reason) {
        attendanceService.rejectOvertime(attendanceLogId, reason);
        return ResponseEntity.noContent().build();
    }
}
