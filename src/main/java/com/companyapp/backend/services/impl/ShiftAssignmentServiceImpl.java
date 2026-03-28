package com.companyapp.backend.services.impl;

import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import com.companyapp.backend.entity.*;
import com.companyapp.backend.enums.AvailabilityStatus;
import com.companyapp.backend.services.exception.*;
import com.companyapp.backend.repository.*;
import com.companyapp.backend.services.AuditLogService; // PŘIDÁNO
import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.ShiftAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final QualificationService qualificationService;
    private final AuditLogService auditLogService; // PŘIDÁNO: Záznam do auditu

    @Override
    @Transactional
    public ShiftAssignmentDto assignShift(UUID shiftId, UUID userId) {
        log.info("Zahajuji proces přiřazení směny {} pro uživatele {}", shiftId, userId);

        Shift shift = shiftRepository.findByIdWithLock(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Směna nebyla nalezena."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nebyl nalezen."));

        long currentCount = shiftAssignmentRepository.countByShiftId(shiftId);
        if (currentCount >= shift.getRequiredCapacity()) {
            throw new CapacityExceededException("Kapacita této konkrétní směny je již vyčerpána (" + shift.getRequiredCapacity() + ").");
        }

        boolean hasAvail = availabilityRepository.existsByUserIdAndAvailableDateAndStatus(
                userId, shift.getStartTime().toLocalDate(), AvailabilityStatus.AVAILABLE);
        if (!hasAvail) {
            throw new AvailabilityNotProvidedException("Zaměstnanec nemá nahlášenou dostupnost na tento den.");
        }

        LocalDateTime checkStart = shift.getStartTime().toLocalDateTime().plusMinutes(35);
        LocalDateTime checkEnd = shift.getEndTime().toLocalDateTime().minusMinutes(35);

        if (checkStart.isAfter(checkEnd)) {
            checkStart = shift.getStartTime().toLocalDateTime().plusMinutes(1);
            checkEnd = shift.getEndTime().toLocalDateTime().minusMinutes(1);
        }

        long overlappingCount = shiftAssignmentRepository.countOverlappingShifts(
                userId, checkStart, checkEnd
        );

        if (overlappingCount > 0) {
            throw new ShiftCollisionException("Zaměstnanec již v tomto čase má jinou směnu (překryv je příliš velký).");
        }

        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setShift(shift);
        assignment.setEmployee(user);
        assignment.setStartTime(shift.getStartTime().toLocalDateTime());
        assignment.setEndTime(shift.getEndTime().toLocalDateTime());

        availabilityRepository.updateStatusByUserIdAndAvailableDate(
                userId, shift.getStartTime().toLocalDate());

        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(assignment);

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "ASSIGN_USER_TO_SHIFT",
                "ShiftAssignment",
                savedAssignment.getId().toString(),
                "Uživatel " + user.getEmail() + " přiřazen na směnu (Stanoviště: " + shift.getStation().getName() + ", Datum: " + shift.getShiftDate() + ")."
        );

        return mapToDto(savedAssignment);
    }

    @Override
    @Transactional
    public void removeAssignmentByShiftAndUser(UUID shiftId, UUID userId) {
        log.info("Odebírám uživatele {} ze směny {}", userId, shiftId);

        // ZÁZNAM DO AUDITU (zaznamenáme před smazáním)
        auditLogService.logAction(
                "REMOVE_USER_FROM_SHIFT",
                "ShiftAssignment",
                shiftId.toString() + "_" + userId.toString(),
                "Uživatel (ID: " + userId + ") odebrán ze směny (ID: " + shiftId + ")."
        );

        shiftAssignmentRepository.deleteByShiftIdAndEmployeeId(shiftId, userId);
    }

    @Override
    @Transactional
    public void removeAssignment(UUID id) {
        if (!shiftAssignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Přiřazení směny neexistuje.");
        }

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "REMOVE_SHIFT_ASSIGNMENT",
                "ShiftAssignment",
                id.toString(),
                "Zrušeno přiřazení (Assignment ID: " + id + ")."
        );

        shiftAssignmentRepository.deleteById(id);
    }

    private ShiftAssignmentDto mapToDto(ShiftAssignment assignment) {
        return ShiftAssignmentDto.builder()
                .id(assignment.getId())
                .shiftId(assignment.getShift().getId())
                .userId(assignment.getEmployee().getId())
                .userName(assignment.getEmployee().getFirstName() + " " + assignment.getEmployee().getLastName())
                .stationName(assignment.getShift().getStation().getName())
                .startTime(assignment.getStartTime().atZone(ZoneId.of("UTC")))
                .endTime(assignment.getEndTime().atZone(ZoneId.of("UTC")))
                .build();
    }
}