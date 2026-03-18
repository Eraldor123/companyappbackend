package com.companyapp.backend.services.impl;

import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;
import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.enums.AvailabilityStatus;
import com.companyapp.backend.services.exception.*;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.UserRepository;
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

        // 1. Načtení entit
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Směna s ID " + shiftId + " nebyla nalezena."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel s ID " + userId + " nebyl nalezen."));

        // 2. Kontrola kapacity a Optimistic Locking
        // Hibernate se o @Version (OptimisticLockException) postará při samotném uložení, 
        // zde ale bráníme byznysovému překročení kapacity.
        long currentAssignmentsCount = shiftAssignmentRepository.countByShiftId(shiftId);
        if (currentAssignmentsCount >= shift.getStation().getCapacityLimit()) {
            throw new CapacityExceededException("Kapacitní limit pro stanoviště " + shift.getStation().getName() + " byl již vyčerpán.");
        }

        // 3. Pravidlo dostupnosti
        boolean hasAvailability = availabilityRepository.existsByUserIdAndDateAndStatus(
                userId,
                shift.getStartTime().toLocalDate()
        );
        if (!hasAvailability) {
            throw new AvailabilityNotProvidedException("Zaměstnanec " + user.getLastName() + " nemá nahlášený volný čas pro tento den.");
        }

        // 4. Kvalifikační předpoklad
        boolean isQualified = qualificationService.verifyUserQualificationForStation(userId, shift.getStation().getId());
        if (!isQualified) {
            throw new MissingQualificationException("Zaměstnanec nemá potřebné zaškolení pro práci na tomto stanovišti.");
        }

        // 5. Overlap Check (Pravidlo nepřekrývání)
        int overlappingCount = shiftAssignmentRepository.countOverlappingShifts(
                userId,
                shift.getStartTime().toLocalDateTime(),
                shift.getEndTime().toLocalDateTime()
        );
        if (overlappingCount > 0) {
            throw new ShiftCollisionException("Vznikla kolize: Zaměstnanec je v tomto čase již přiřazen na jiné stanoviště.");
        }

        // (Poznámka: Metoda pro ověření maximálních odpracovaných hodin vůči ContractType by byla volána zde)

        // 6. Uložení a vytvoření vazby
        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setShift(shift);
        assignment.setUser(user);

        // Změníme stav dostupnosti na "konzumováno"
        availabilityRepository.updateStatusByUserIdAndDate(userId, shift.getStartTime().toLocalDate());

        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(assignment);
        log.info("Uživatel {} byl úspěšně alokován na směnu {}", userId, shiftId);

        return mapToDto(savedAssignment);
    }

    @Override
    public void removeAssignment(UUID shiftAssignmentId) {
        // Implementace pro odebrání přiřazení (s reverzí dostupnosti a případným upozorněním manažera)
    }

    private ShiftAssignmentDto mapToDto(ShiftAssignment assignment) {
        // Zde využijeme MapStruct nebo manuální buildování DTO pro zamezení LazyInitializationException
        return ShiftAssignmentDto.builder()
                .id(assignment.getId())
                .shiftId(assignment.getShift().getId())
                .userId(assignment.getUser().getId())
                .userName(assignment.getUser().getFirstName() + " " + assignment.getUser().getLastName())
                .stationName(assignment.getShift().getStation().getName())
                .startTime(assignment.getShift().getStartTime())
                .endTime(assignment.getShift().getEndTime())
                .build();
    }

    // Zde by pokračovala implementace metody removeAssignment...
}