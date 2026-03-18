package com.companyapp.backend.services.impl;

import com.companyapp.backend.repository.AttendanceLogRepository;
import com.companyapp.backend.services.AttendanceProcessingService;
import com.companyapp.backend.services.TimeCalculationService;
import com.companyapp.backend.services.dto.response.AttendanceLogDto;
import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceProcessingServiceImpl implements AttendanceProcessingService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final TimeCalculationService timeCalculationService;

    @Override
    @Transactional
    public AttendanceLogDto clockIn(UUID userId, UUID shiftAssignmentId, Instant clockInTime) {
        // Vytvoří AttendanceLog a napevno ho sváže s ShiftAssignment přes cizí klíč
        return null;
    }

    @Override
    @Transactional
    public AttendanceLogDto clockOut(UUID userId, Instant clockOutTime) { return null; }

    @Override
    @Transactional
    public void rejectOvertime(UUID attendanceLogId, String reason) {}

    @Override
    @Transactional
    public List<ShiftAssignmentDto> splitShiftAssignment(UUID shiftAssignmentId, ZonedDateTime breakStart, ZonedDateTime breakEnd) { return null; }
}