package com.companyapp.backend.services.impl;

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

    @Override
    @Transactional
    public AttendanceLogDto clockIn(UUID userId, UUID shiftAssignmentId, Instant clockInTime) {
        log.info("Uživatel {} si pípl příchod na směnu {}", userId, shiftAssignmentId);

        ShiftAssignment assignment = shiftAssignmentRepository.findById(shiftAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Přiřazení směny nenalezeno."));

        // Zkontrolujeme, jestli už nemá existující záznam, abychom nevytvořili duplikát
        attendanceLogRepository.findByShiftAssignmentId(assignment.getId()).ifPresent(log -> {
            throw new IllegalStateException("K této směně již existuje záznam docházky.");
        });

        AttendanceLog logEntity = new AttendanceLog();
        logEntity.setShiftAssignment(assignment);

        // Převod Instant (z terminálu) na LocalDateTime pro tvou entitu (která má typ LocalDateTime)
        logEntity.setClockIn(ZonedDateTime.ofInstant(clockInTime, ZoneId.of("UTC")).toLocalDateTime());
        logEntity.setIsUnusual(false);
        logEntity.setManagerApproved(true);

        AttendanceLog savedLog = attendanceLogRepository.save(logEntity);
        return mapToDto(savedLog);
    }
    @Override
    @Transactional
    public AttendanceLogDto clockOut(UUID userId, Instant clockOutTime) {
        log.info("Uživatel {} si pípl odchod", userId);

        // 1. Najdeme otevřený záznam uživatele
        AttendanceLog logEntity = attendanceLogRepository.findOpenLogForUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenalezen otevřený příchod pro tohoto uživatele."));

        // 2. Nastavíme čas odchodu
        logEntity.setClockOut(ZonedDateTime.ofInstant(clockOutTime, ZoneId.of("UTC")).toLocalDateTime());

        // 3. Výpočet čistého času pomocí TimeCalculationService
        ZonedDateTime start = logEntity.getClockIn().atZone(ZoneId.of("UTC"));
        ZonedDateTime end = logEntity.getClockOut().atZone(ZoneId.of("UTC"));
        Duration netWorkTime = timeCalculationService.calculateNetWorkTime(start, end);

        logEntity.setNetTimeMinutes((int) netWorkTime.toMinutes());

        // 4. Spočítáme pauzu (Hrubý čas v minutách mínus Čistý čas)
        int grossMinutes = (int) Duration.between(start, end).toMinutes();
        logEntity.setDeductedPauseMinutes(grossMinutes - logEntity.getNetTimeMinutes());

        // 5. Uložíme a vrátíme DTO
        return mapToDto(attendanceLogRepository.save(logEntity));
    }

    @Override
    @Transactional
    public void rejectOvertime(UUID attendanceLogId, String reason) {
        AttendanceLog logEntity = attendanceLogRepository.findById(attendanceLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Záznam docházky nenalezen."));

        logEntity.setManagerApproved(false);
        // Poznámka: Entita AttendanceLog aktuálně nemá pole 'reviewNote' / 'reason'.
        // Pokud ho tam chceš ukládat, musíš ho přidat do třídy AttendanceLog.java.

        attendanceLogRepository.save(logEntity);
    }

    @Override
    @Transactional
    public List<ShiftAssignmentDto> splitShiftAssignment(UUID shiftAssignmentId, ZonedDateTime breakStart, ZonedDateTime breakEnd) {
        throw new UnsupportedOperationException("Tato metoda zatím není implementována.");
    }

    @Override
    @Transactional
    public AttendanceLogDto processTerminalAction(UUID userId) {
        Instant now = Instant.now();
        // Získáme aktuální čas pro porovnání s databází
        java.time.LocalDateTime localNow = ZonedDateTime.ofInstant(now, ZoneId.of("UTC")).toLocalDateTime();

        // 1. SCÉNÁŘ: Uživatel je v práci a chce odejít (má otevřený záznam docházky)
        java.util.Optional<AttendanceLog> openLog = attendanceLogRepository.findOpenLogForUser(userId);
        if (openLog.isPresent()) {
            log.info("Nalezen otevřený záznam. Provádím ODCHOD pro uživatele {}.", userId);
            return clockOut(userId, now);
        }

        // 2. SCÉNÁŘ: Uživatel není v práci, zkusíme najít jeho aktuální směnu
        // Nastavíme toleranci: Může si pípnout nejdříve 2 hodiny před začátkem směny a nejpozději 2 hodiny po jejím konci
        java.time.LocalDateTime windowStart = localNow.minusHours(2);
        java.time.LocalDateTime windowEnd = localNow.plusHours(2);

        java.util.List<ShiftAssignment> assignments = shiftAssignmentRepository.findCurrentAssignments(userId, windowStart, windowEnd);

        if (assignments.isEmpty()) {
            throw new IllegalStateException("Nemáte momentálně naplánovanou žádnou směnu.");
        }

        // Vezmeme první nalezenou směnu (v praxi by se dalo řešit, co když jich má víc ve stejný čas)
        ShiftAssignment currentAssignment = assignments.get(0);

        log.info("Nalezena dnešní směna. Provádím PŘÍCHOD pro uživatele {}.", userId);
        return clockIn(userId, currentAssignment.getId(), now);
    }

    private AttendanceLogDto mapToDto(AttendanceLog logEntity) {
        return AttendanceLogDto.builder()
                .id(logEntity.getId())
                .shiftAssignmentId(logEntity.getShiftAssignment().getId())
                // .userName(...) - Zde bys napojil jméno z User entity
                .clockInTime(logEntity.getClockIn() != null ? logEntity.getClockIn().atZone(ZoneId.of("UTC")).toInstant() : null)
                .clockOutTime(logEntity.getClockOut() != null ? logEntity.getClockOut().atZone(ZoneId.of("UTC")).toInstant() : null)
                .status(logEntity.getManagerApproved() ? "APPROVED" : "NEEDS_APPROVAL")
                .build();
    }
}