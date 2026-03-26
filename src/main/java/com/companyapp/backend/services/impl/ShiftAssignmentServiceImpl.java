package com.companyapp.backend.services.impl;

import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import com.companyapp.backend.entity.*;
import com.companyapp.backend.enums.AvailabilityStatus;
import com.companyapp.backend.services.exception.*;
import com.companyapp.backend.repository.*;
import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.ShiftAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public ShiftAssignmentDto assignShift(UUID shiftId, UUID userId) {
        log.info("Zahajuji proces přiřazení směny {} pro uživatele {}", shiftId, userId);

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Směna nebyla nalezena."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nebyl nalezen."));

        // 2. Kontrola kapacity
        long currentCount = shiftAssignmentRepository.countByShiftId(shiftId);
        if (currentCount >= shift.getStation().getCapacityLimit()) {
            throw new CapacityExceededException("Kapacita stanoviště vyčerpána.");
        }

        // 3. Pravidlo dostupnosti
        boolean hasAvail = availabilityRepository.existsByUserIdAndAvailableDateAndStatus(
                userId, shift.getStartTime().toLocalDate(), AvailabilityStatus.AVAILABLE);
        if (!hasAvail) {
            throw new AvailabilityNotProvidedException("Zaměstnanec nemá nahlášenou dostupnost.");
        }

        // --- 4. OPRAVENO: Nový název metody v QualificationService ---
        if (!qualificationService.isUserQualifiedForStation(userId, shift.getStation().getId())) {
            throw new MissingQualificationException("Chybí potřebná kvalifikace pro toto stanoviště.");
        }

        // 5. Overlap Check
        long overlappingCount = shiftAssignmentRepository.countOverlappingShifts(
                userId,
                shift.getStartTime().toLocalDateTime(),
                shift.getEndTime().toLocalDateTime()
        );

        if (overlappingCount > 0) {
            throw new ShiftCollisionException("Zaměstnanec již v tomto čase má jinou směnu.");
        }

        // 6. Act - Uložení
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setShift(shift);
        assignment.setEmployee(user);
        assignment.setStartTime(shift.getStartTime().toLocalDateTime());
        assignment.setEndTime(shift.getEndTime().toLocalDateTime());

        availabilityRepository.updateStatusByUserIdAndAvailableDate(
                userId, shift.getStartTime().toLocalDate());

        return mapToDto(shiftAssignmentRepository.save(assignment));
    }

    private ShiftAssignmentDto mapToDto(ShiftAssignment assignment) {
        return ShiftAssignmentDto.builder()
                .id(assignment.getId())
                .shiftId(assignment.getShift().getId())
                .userId(assignment.getEmployee().getId())
                .userName(assignment.getEmployee().getFirstName() + " " + assignment.getEmployee().getLastName())
                .stationName(assignment.getShift().getStation().getName())
                .startTime(assignment.getStartTime().atZone(java.time.ZoneId.of("UTC")))
                .endTime(assignment.getEndTime().atZone(java.time.ZoneId.of("UTC")))
                .build();
    }

    @Override
    @Transactional
    public void removeAssignment(UUID id) {
        log.info("Odebírám uživatele z přiřazení směny: {}", id);
        if (!shiftAssignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Přiřazení směny neexistuje.");
        }
        shiftAssignmentRepository.deleteById(id);
    }
}