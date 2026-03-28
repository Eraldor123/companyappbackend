package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.AttendanceLogRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.services.AuditLogService; // PŘIDÁNO
import com.companyapp.backend.services.ShiftService;
import com.companyapp.backend.services.dto.request.ShiftUpdateRequest;
import com.companyapp.backend.services.dto.response.ShiftDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final AuditLogService auditLogService; // PŘIDÁNO: Záznam do auditu

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

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "UPDATE_SHIFT",
                "Shift",
                savedShift.getId().toString(),
                "Upravena směna (Kapacita: " + savedShift.getRequiredCapacity() + "). Stanoviště: " + savedShift.getStation().getName()
        );

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
                .withZoneSameInstant(ZoneId.of("Europe/Prague"))
                .withHour(14)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .withZoneSameInstant(ZoneId.of("UTC"));

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

            // ZÁZNAM DO AUDITU
            auditLogService.logAction(
                    "SPLIT_SHIFT",
                    "Shift",
                    originalShift.getId().toString(),
                    "Směna rozdělena ve 14:00. Nová směna ID: " + newShift.getId()
            );

        } else {
            throw new IllegalStateException("Tuto směnu nelze rozdělit (neobsahuje čas 14:00).");
        }
    }

    @Override
    @Transactional
    public void deleteShift(UUID id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Směna s ID " + id + " nebyla nalezena."));

        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftDateBetween(shift.getShiftDate(), shift.getShiftDate())
                .stream()
                .filter(a -> a.getShift().getId().equals(id))
                .toList();

        for (ShiftAssignment assignment : assignments) {
            if (attendanceLogRepository.findByShiftAssignmentId(assignment.getId()).isPresent()) {
                throw new IllegalStateException("Směnu nelze smazat, protože zaměstnanci na ní již mají zaznamenanou docházku.");
            }
        }

        if (!assignments.isEmpty()) {
            shiftAssignmentRepository.deleteAll(assignments);
        }

        // Uložíme si info pro log, než objekt smažeme
        String dateAndStation = shift.getShiftDate() + " na stanovišti " + shift.getStation().getName();

        shiftRepository.delete(shift);

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "DELETE_SHIFT",
                "Shift",
                id.toString(),
                "Kompletně smazána směna z " + dateAndStation + " (odebráno " + assignments.size() + " uživatelů)."
        );
    }
}