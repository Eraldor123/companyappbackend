package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.AttendanceLogRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.services.AuditLogService;
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
    private final AuditLogService auditLogService;

    // KONSTANTY PRO ODSTRANĚNÍ DUPLIKACÍ (java:S1192)
    private static final String ENTITY_NAME = "Shift";
    private static final String SHIFT_NOT_FOUND = "Směna nebyla nalezena.";
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    private static final ZoneId PRAGUE_ZONE = ZoneId.of("Europe/Prague");

    @Override
    @Transactional
    public ShiftDto updateShift(UUID id, ShiftUpdateRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SHIFT_NOT_FOUND));

        shift.setStartTime(request.getStartTime().atZone(UTC_ZONE));
        shift.setEndTime(request.getEndTime().atZone(UTC_ZONE));
        shift.setRequiredCapacity(request.getRequiredCapacity());
        shift.setDescription(request.getDescription());

        Shift savedShift = shiftRepository.save(shift);

        auditLogService.logAction(
                "UPDATE_SHIFT",
                ENTITY_NAME,
                savedShift.getId().toString(),
                "Upravena směna (Kapacita: " + savedShift.getRequiredCapacity() + "). Stanoviště: " + savedShift.getStation().getName()
        );

        return mapToDto(savedShift);
    }

    @Override
    @Transactional
    public void splitShift(UUID shiftId) {
        Shift originalShift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException(SHIFT_NOT_FOUND));

        ZonedDateTime splitTime = originalShift.getStartTime()
                .withZoneSameInstant(PRAGUE_ZONE)
                .withHour(14)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .withZoneSameInstant(UTC_ZONE);

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

            auditLogService.logAction(
                    "SPLIT_SHIFT",
                    ENTITY_NAME,
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
                .orElseThrow(() -> new ResourceNotFoundException(SHIFT_NOT_FOUND));

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

        String dateAndStation = shift.getShiftDate() + " na stanovišti " + shift.getStation().getName();
        shiftRepository.delete(shift);

        auditLogService.logAction(
                "DELETE_SHIFT",
                ENTITY_NAME,
                id.toString(),
                "Kompletně smazána směna z " + dateAndStation + " (odebráno " + assignments.size() + " uživatelů)."
        );
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
}