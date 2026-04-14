package com.companyapp.backend.services.impl;

import com.companyapp.backend.config.CheckOwnership;
import com.companyapp.backend.entity.AttendanceLog;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.AttendanceLogRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.services.AttendanceProcessingService;
import com.companyapp.backend.services.TimeCalculationService;
import com.companyapp.backend.services.dto.response.AttendanceLogDto;
import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired; // PŘIDÁNO
import org.springframework.context.annotation.Lazy; // PŘIDÁNO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceProcessingServiceImpl implements AttendanceProcessingService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final TimeCalculationService timeCalculationService;

    // --- OPRAVA AUDITU: Vstříknutí sebe sama pro vynucení AOP Proxy ---
    @Lazy
    @Autowired
    private AttendanceProcessingService self;

    @Override
    @Transactional
    // PŘIDÁNA ANOTACE @CheckOwnership
    public AttendanceLogDto clockIn(@CheckOwnership UUID userId, UUID shiftAssignmentId, Instant clockInTime) {
        log.info("Uživatel {} si pípl příchod na směnu {}", userId, shiftAssignmentId);

        ShiftAssignment assignment = shiftAssignmentRepository.findById(shiftAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Přiřazení směny nenalezeno."));

        attendanceLogRepository.findByShiftAssignmentId(assignment.getId()).ifPresent(log -> {
            throw new IllegalStateException("K této směně již existuje záznam docházky.");
        });

        AttendanceLog logEntity = new AttendanceLog();
        logEntity.setShiftAssignment(assignment);
        logEntity.setClockIn(ZonedDateTime.ofInstant(clockInTime, ZoneId.of("UTC")).toLocalDateTime());
        logEntity.setIsUnusual(false);
        logEntity.setManagerApproved(true);

        AttendanceLog savedLog = attendanceLogRepository.save(logEntity);
        return mapToDto(savedLog);
    }

    @Override
    @Transactional
    // PŘIDÁNA ANOTACE @CheckOwnership
    public AttendanceLogDto clockOut(@CheckOwnership UUID userId, Instant clockOutTime) {
        log.info("Uživatel {} si pípl odchod", userId);

        AttendanceLog logEntity = attendanceLogRepository.findOpenLogForUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenalezen otevřený příchod pro tohoto uživatele."));

        logEntity.setClockOut(ZonedDateTime.ofInstant(clockOutTime, ZoneId.of("UTC")).toLocalDateTime());

        ZonedDateTime start = logEntity.getClockIn().atZone(ZoneId.of("UTC"));
        ZonedDateTime end = logEntity.getClockOut().atZone(ZoneId.of("UTC"));
        Duration netWorkTime = timeCalculationService.calculateNetWorkTime(start, end);

        logEntity.setNetTimeMinutes((int) netWorkTime.toMinutes());

        int grossMinutes = (int) Duration.between(start, end).toMinutes();
        logEntity.setDeductedPauseMinutes(grossMinutes - logEntity.getNetTimeMinutes());

        return mapToDto(attendanceLogRepository.save(logEntity));
    }

    @Override
    @Transactional
    public void rejectOvertime(UUID attendanceLogId, String reason) {
        AttendanceLog logEntity = attendanceLogRepository.findById(attendanceLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Záznam docházky nenalezen."));

        logEntity.setManagerApproved(false);
        attendanceLogRepository.save(logEntity);
    }

    @Override
    @Transactional
    public List<ShiftAssignmentDto> splitShiftAssignment(UUID shiftAssignmentId, ZonedDateTime breakStart, ZonedDateTime breakEnd) {
        throw new UnsupportedOperationException("Tato metoda zatím není implementována.");
    }

    @Override
    @Transactional
    // PŘIDÁNA ANOTACE @CheckOwnership
    public AttendanceLogDto processTerminalAction(@CheckOwnership UUID userId) {
        Instant now = Instant.now();
        java.time.LocalDateTime localNow = ZonedDateTime.ofInstant(now, ZoneId.of("UTC")).toLocalDateTime();

        java.util.Optional<AttendanceLog> openLog = attendanceLogRepository.findOpenLogForUser(userId);
        if (openLog.isPresent()) {
            log.info("Nalezen otevřený záznam. Provádím ODCHOD pro uživatele {}.", userId);

            // --- OPRAVA: Voláme to přes 'self', takže AOP Proxy zachytí anotaci! ---
            return self.clockOut(userId, now);
        }

        java.time.LocalDateTime windowStart = localNow.minusHours(2);
        java.time.LocalDateTime windowEnd = localNow.plusHours(2);

        java.util.List<ShiftAssignment> assignments = shiftAssignmentRepository.findCurrentAssignments(userId, windowStart, windowEnd);

        if (assignments.isEmpty()) {
            throw new IllegalStateException("Nemáte momentálně naplánovanou žádnou směnu.");
        }

        ShiftAssignment currentAssignment = assignments.get(0);

        log.info("Nalezena dnešní směna. Provádím PŘÍCHOD pro uživatele {}.", userId);

        // --- OPRAVA: Voláme to přes 'self', takže AOP Proxy zachytí anotaci! ---
        return self.clockIn(userId, currentAssignment.getId(), now);
    }

    private AttendanceLogDto mapToDto(AttendanceLog logEntity) {
        return AttendanceLogDto.builder()
                .id(logEntity.getId())
                .shiftAssignmentId(logEntity.getShiftAssignment().getId())
                .clockInTime(logEntity.getClockIn() != null ? logEntity.getClockIn().atZone(ZoneId.of("UTC")).toInstant() : null)
                .clockOutTime(logEntity.getClockOut() != null ? logEntity.getClockOut().atZone(ZoneId.of("UTC")).toInstant() : null)
                .status(logEntity.getManagerApproved() ? "APPROVED" : "NEEDS_APPROVAL")
                .build();
    }
}