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

    @Override
    @Transactional
    public ShiftAssignmentDto assignShift(UUID shiftId, UUID userId) {
        log.info("Zahajuji proces přiřazení směny {} pro uživatele {}", shiftId, userId);

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Směna nebyla nalezena."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nebyl nalezen."));

        // 1. KONTROLA KAPACITY (Upraveno na kapacitu konkrétní směny)
        // Teď už se nedíváme na station.getCapacityLimit, ale na to, co jsi nastavil u směny
        long currentCount = shiftAssignmentRepository.countByShiftId(shiftId);
        if (currentCount >= shift.getRequiredCapacity()) {
            throw new CapacityExceededException("Kapacita této konkrétní směny je již vyčerpána (" + shift.getRequiredCapacity() + ").");
        }

        // 2. Pravidlo dostupnosti
        boolean hasAvail = availabilityRepository.existsByUserIdAndAvailableDateAndStatus(
                userId, shift.getStartTime().toLocalDate(), AvailabilityStatus.AVAILABLE);
        if (!hasAvail) {
            throw new AvailabilityNotProvidedException("Zaměstnanec nemá nahlášenou dostupnost na tento den.");
        }

        // 3. Kontrola kvalifikace
        if (!qualificationService.isUserQualifiedForStation(userId, shift.getStation().getId())) {
            throw new MissingQualificationException("Chybí potřebná kvalifikace pro toto stanoviště.");
        }

        // 4. Overlap Check (S TOLERANCÍ PRO PŘEDÁVÁNÍ SMĚN)
        LocalDateTime checkStart = shift.getStartTime().toLocalDateTime().plusMinutes(35);
        LocalDateTime checkEnd = shift.getEndTime().toLocalDateTime().minusMinutes(35);

        if (checkStart.isAfter(checkEnd)) {
            checkStart = shift.getStartTime().toLocalDateTime().plusMinutes(1);
            checkEnd = shift.getEndTime().toLocalDateTime().minusMinutes(1);
        }

        long overlappingCount = shiftAssignmentRepository.countOverlappingShifts(
                userId,
                checkStart,
                checkEnd
        );

        if (overlappingCount > 0) {
            throw new ShiftCollisionException("Zaměstnanec již v tomto čase má jinou směnu (překryv je příliš velký).");
        }

        // 5. Uložení přiřazení
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setShift(shift);
        assignment.setEmployee(user);
        assignment.setStartTime(shift.getStartTime().toLocalDateTime());
        assignment.setEndTime(shift.getEndTime().toLocalDateTime());

        // Volitelně: Aktualizace stavu dostupnosti (např. na PLANNED)
        availabilityRepository.updateStatusByUserIdAndAvailableDate(
                userId, shift.getStartTime().toLocalDate());

        return mapToDto(shiftAssignmentRepository.save(assignment));
    }

    // --- NOVÁ METODA PRO MAZÁNÍ PODLE SHIFT + USER ---
    @Override
    @Transactional
    public void removeAssignmentByShiftAndUser(UUID shiftId, UUID userId) {
        log.info("Odebírám uživatele {} ze směny {}", userId, shiftId);

        // Důležité: metoda v repository musí existovat (deleteByShiftIdAndEmployeeId)
        shiftAssignmentRepository.deleteByShiftIdAndEmployeeId(shiftId, userId);
    }

    @Override
    @Transactional
    public void removeAssignment(UUID id) {
        log.info("Odebírám přiřazení podle ID: {}", id);
        if (!shiftAssignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Přiřazení směny neexistuje.");
        }
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