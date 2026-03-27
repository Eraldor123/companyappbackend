package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.services.ShiftService;
import com.companyapp.backend.services.dto.request.ShiftUpdateRequest;
import com.companyapp.backend.services.dto.response.ShiftDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository; // PŘIDÁNO: Nutné pro mazání přiřazení

    @Override
    @Transactional
    public ShiftDto updateShift(UUID id, ShiftUpdateRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Směna nebyla nalezena."));

        shift.setStartTime(request.getStartTime().atZone(ZoneId.of("UTC")));
        shift.setEndTime(request.getEndTime().atZone(ZoneId.of("UTC")));
        shift.setRequiredCapacity(request.getRequiredCapacity());
        shift.setDescription(request.getDescription());

        Shift savedShift = shiftRepository.save(shift);
        return mapToDto(savedShift);
    }

    private ShiftDto mapToDto(Shift shift) {
        return ShiftDto.builder()
                .id(shift.getId())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .requiredCapacity(shift.getRequiredCapacity())
                .stationId(shift.getStation().getId())
                .build();
    }

    @Override
    @Transactional
    public void splitShift(UUID shiftId) {
        Shift originalShift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Směna nenalezena."));

        ZonedDateTime splitTime = originalShift.getStartTime()
                .withHour(14)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        if (originalShift.getStartTime().isBefore(splitTime) && originalShift.getEndTime().isAfter(splitTime)) {
            Shift newShift = new Shift();
            newShift.setStation(originalShift.getStation());
            newShift.setRequiredCapacity(originalShift.getRequiredCapacity());
            newShift.setStartTime(splitTime);
            newShift.setEndTime(originalShift.getEndTime());
            newShift.setShiftDate(originalShift.getShiftDate());

            shiftRepository.save(newShift);

            originalShift.setEndTime(splitTime);
            shiftRepository.save(originalShift);
        } else {
            throw new IllegalStateException("Tuto směnu nelze rozdělit (neobsahuje čas 14:00).");
        }
    }

    @Override
    @Transactional
    public void deleteShift(UUID id) {
        // 1. Ověříme, zda směna existuje
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Směna s ID " + id + " nebyla nalezena."));

        // 2. Najdeme a smažeme všechna přiřazení (assignments) pro tuto konkrétní směnu
        // Použijeme efektivnější způsob: najdeme přiřazení podle směny
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftDateBetween(shift.getShiftDate(), shift.getShiftDate())
                .stream()
                .filter(a -> a.getShift().getId().equals(id))
                .toList();

        if (!assignments.isEmpty()) {
            shiftAssignmentRepository.deleteAll(assignments);
        }

        // 3. Smažeme samotnou směnu
        shiftRepository.delete(shift);
    }
}