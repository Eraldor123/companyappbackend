package com.companyapp.backend.services.impl;

import com.companyapp.backend.config.CheckOwnership;
import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.ShiftAssignmentService;
import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import com.companyapp.backend.services.exception.AvailabilityNotProvidedException;
import com.companyapp.backend.services.exception.CapacityExceededException;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AuditLogService auditLogService;

    /**
     * OPRAVA java:S1124 & java:S115:
     * 1. Změněno pořadí modifikátorů na 'static final'.
     * 2. Přejmenováno na ENTITY_NAME (UPPER_SNAKE_CASE).
     */
    private static final String ENTITY_NAME = "ShiftAssignment";

    private static final String SHIFT_NOT_FOUND = "Směna nebyla nalezena.";
    private static final String USER_NOT_FOUND = "Uživatel nebyl nalezen.";

    @Override
    @Transactional
    public ShiftAssignmentDto assignShift(UUID shiftId, @CheckOwnership UUID userId) {
        log.info("Zahajuji proces přiřazení směny {} pro uživatele {}", shiftId, userId);

        // 1. Zjistit, jestli už uživatel na směně není (Prevence unique constraint violation)
        boolean alreadyAssigned = shiftAssignmentRepository.existsByShiftIdAndEmployeeId(shiftId, userId);
        if (alreadyAssigned) {
            log.warn("Uživatel {} už je přiřazen ke směně {}. Přeskakuji.", userId, shiftId);
            return null; // Tímto se transakce nepřeruší, pouze se tento krok ignoruje
        }

        Shift shift = shiftRepository.findByIdWithLock(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException(SHIFT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

        long currentCount = shiftAssignmentRepository.countByShiftId(shiftId);
        if (currentCount >= shift.getRequiredCapacity()) {
            throw new CapacityExceededException("Kapacita této konkrétní směny je již vyčerpána (" + shift.getRequiredCapacity() + ").");
        }

        boolean hasAvail = availabilityRepository.existsByUserIdAndAvailableDate(
                userId, shift.getStartTime().toLocalDate());
        if (!hasAvail) {
            throw new AvailabilityNotProvidedException("Zaměstnanec nemá nahlášenou dostupnost na tento den.");
        }

        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setShift(shift);
        assignment.setEmployee(user);
        assignment.setStartTime(shift.getStartTime().toLocalDateTime());
        assignment.setEndTime(shift.getEndTime().toLocalDateTime());

        availabilityRepository.updateStatusByUserIdAndAvailableDate(
                userId, shift.getStartTime().toLocalDate());

        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(assignment);

        auditLogService.logAction(
                "ASSIGN_USER_TO_SHIFT",
                ENTITY_NAME,
                savedAssignment.getId().toString(),
                "Uživatel " + user.getEmail() + " přiřazen na směnu (Stanoviště: " + shift.getStation().getName() + ", Datum: " + shift.getShiftDate() + ")."
        );

        return mapToDto(savedAssignment);
    }

    @Override
    @Transactional
    public void removeAssignmentByShiftAndUser(UUID shiftId, @CheckOwnership UUID userId) {
        log.info("Odebírám uživatele {} ze směny {}", userId, shiftId);

        auditLogService.logAction(
                "REMOVE_USER_FROM_SHIFT",
                ENTITY_NAME,
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

        auditLogService.logAction(
                "REMOVE_SHIFT_ASSIGNMENT",
                ENTITY_NAME,
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