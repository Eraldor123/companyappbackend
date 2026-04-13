package com.companyapp.backend.controller;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.OperatingHoursService;
import com.companyapp.backend.services.AutoPlanService;
import com.companyapp.backend.services.dto.request.AutoPlanRequestDto;
import com.companyapp.backend.services.dto.response.AssignedUserDto;
import com.companyapp.backend.services.dto.response.PlannerUserDto;
import com.companyapp.backend.services.dto.response.ScheduleShiftDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'MANAGEMENT')")
public class ScheduleController {

    private final OperatingHoursService operatingHoursService;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AutoPlanService autoPlanService;

    @GetMapping("/week-view")
    public ResponseEntity<?> getWeeklySchedule(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (!operatingHoursService.isAnyOperatingHoursPresent()) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY)
                    .body(Map.of("message", "V systému není nastavena otevírací doba. Kalendář nelze zobrazit."));
        }

        Map<String, Object> response = new HashMap<>();
        List<Object> days = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            days.add(operatingHoursService.getOperatingHoursForDate(current));
            current = current.plusDays(1);
        }
        response.put("days", days);

        List<Shift> rawShifts = shiftRepository.findByShiftDateBetween(startDate, endDate);
        List<ShiftAssignment> rawAssignments = shiftAssignmentRepository.findByShiftDateBetween(startDate, endDate);

        // SUPER-POJISTKA proti smazaným (Ghost) uživatelům
        Map<java.util.UUID, List<AssignedUserDto>> assignmentsByShift = rawAssignments.stream()
                .filter(sa -> sa.getShift() != null && sa.getEmployee() != null)
                .collect(Collectors.groupingBy(
                        sa -> sa.getShift().getId(),
                        Collectors.mapping(sa -> {
                            try {
                                return AssignedUserDto.builder()
                                        .userId(sa.getEmployee().getId())
                                        .name((sa.getEmployee().getFirstName() != null ? sa.getEmployee().getFirstName() : "") + " " +
                                                (sa.getEmployee().getLastName() != null ? sa.getEmployee().getLastName() : ""))
                                        .build();
                            } catch (EntityNotFoundException e) {
                                // Uživatel neexistuje, NESMÍME už použít sa.getEmployee() vůbec k ničemu!
                                return AssignedUserDto.builder()
                                        .userId(null) // <--- FINÁLNÍ OPRAVA ZDE
                                        .name("Smazaný uživatel")
                                        .build();
                            }
                        }, Collectors.toList())
                ));

        List<ScheduleShiftDto> shifts = rawShifts.stream()
                .filter(s -> {
                    try {
                        return s.getStation() != null && s.getStation().getName() != null;
                    } catch (EntityNotFoundException e) {
                        return false; // Stanice byla smazána
                    }
                })
                .map(s -> ScheduleShiftDto.builder()
                        .id(s.getId())
                        .stationId(s.getStation().getId())
                        .templateId(s.getTemplate() != null ? s.getTemplate().getId() : null)
                        .shiftDate(s.getShiftDate())
                        .startTime(s.getStartTime() != null ? s.getStartTime().toString() : "00:00")
                        .endTime(s.getEndTime() != null ? s.getEndTime().toString() : "00:00")
                        .requiredCapacity(s.getRequiredCapacity() != null ? s.getRequiredCapacity() : 1)
                        .description(s.getDescription() != null ? s.getDescription() : "")
                        .assignedUsers(assignmentsByShift.getOrDefault(s.getId(), new ArrayList<>()))
                        .build()
                ).collect(Collectors.toList());

        response.put("shifts", shifts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-users")
    public ResponseEntity<List<PlannerUserDto>> getAvailableUsersForWeek(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<com.companyapp.backend.entity.Availability> availabilities = availabilityRepository.findByDateRange(startDate, endDate);

        List<java.util.UUID> userIds = availabilities.stream()
                .map(com.companyapp.backend.entity.Availability::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<com.companyapp.backend.entity.User> activeUsers = userRepository.findAllActiveUsersWithDetails().stream()
                .filter(u -> userIds.contains(u.getId()))
                .collect(Collectors.toList());

        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<ShiftAssignment> monthAssignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(userIds, monthStart, monthEnd);
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneId.of("UTC"));

        List<PlannerUserDto> result = new ArrayList<>();

        for (com.companyapp.backend.entity.User user : activeUsers) {
            List<Integer> qualifiedIds = user.getQualifiedStations().stream()
                    .filter(st -> {
                        try { return st != null && st.getName() != null; }
                        catch(EntityNotFoundException e) { return false; }
                    })
                    .map(com.companyapp.backend.entity.Station::getId)
                    .collect(Collectors.toList());

            Map<String, String> weekAvail = new HashMap<>();
            availabilities.stream().filter(a -> a.getUserId().equals(user.getId())).forEach(a -> {
                String type = (a.isMorning() && a.isAfternoon()) ? "CELÝ DEN" : (a.isMorning() ? "DOP" : "ODP");
                weekAvail.put(a.getAvailableDate().toString(), type);
            });

            int planned = 0;
            int completed = 0;
            for (ShiftAssignment sa : monthAssignments) {
                try {
                    if (sa.getEmployee() != null && sa.getEmployee().getId().equals(user.getId()) && sa.getEndTime() != null) {
                        if (sa.getEndTime().isBefore(now)) {
                            completed++;
                        } else {
                            planned++;
                        }
                    }
                } catch (EntityNotFoundException e) {
                    // Ignorujeme rozbité záznamy
                }
            }

            result.add(PlannerUserDto.builder()
                    .userId(user.getId())
                    .name((user.getFirstName() != null ? user.getFirstName() : "") + " " + (user.getLastName() != null ? user.getLastName() : ""))
                    .qualifiedStationIds(qualifiedIds)
                    .weekAvailability(weekAvail)
                    .plannedShiftsThisMonth(planned)
                    .completedShiftsThisMonth(completed)
                    .build());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/auto-plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> runAutoPlan(@RequestBody AutoPlanRequestDto request) {
        if (!operatingHoursService.isAnyOperatingHoursPresent()) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY)
                    .body(Map.of("message", "Pro spuštění automatického plánování musí být nastavena otevírací doba."));
        }
        autoPlanService.runAutoPlanning(request);
        return ResponseEntity.ok().build();
    }
}