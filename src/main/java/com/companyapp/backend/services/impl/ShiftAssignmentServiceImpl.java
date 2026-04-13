package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.ShiftAssignmentService;
import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import com.companyapp.backend.services.exception.AvailabilityNotProvidedException;
import com.companyapp.backend.services.exception.CapacityExceededException;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final QualificationService qualificationService;
    private final AuditLogService auditLogService;

    private final static String entityName = "ShiftAssignment";

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

        boolean hasAvail = availabilityRepository.existsByUserIdAndAvailableDate(
                userId, shift.getStartTime().toLocalDate());
        if (!hasAvail) {
            throw new AvailabilityNotProvidedException("Zaměstnanec nemá nahlášenou dostupnost na tento den.");
        }

        // TADY BYLA PŘÍSNÁ BLOKACE PŘEKRYVU SMĚN - Byla odstraněna, aby manažer
        // mohl (v případě potřeby) přiřadit uživatele na více stanovišť ve stejný čas.
        // Varování na kolizi nyní řeší frontend pomocí ScheduleControlleru.

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
                entityName,
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
                entityName,
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
                entityName,
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