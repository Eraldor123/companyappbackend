package com.companyapp.backend.controller;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.entity.User;
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
import org.springframework.data.domain.Page; // PŘIDÁNO
import org.springframework.data.domain.Pageable; // PŘIDÁNO
import org.springframework.data.web.PageableDefault; // PŘIDÁNO
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        // FÁZE 2: Využívá optimalizované findByShiftDateBetween s EntityGraph v ShiftRepository
        List<Shift> rawShifts = shiftRepository.findByShiftDateBetween(startDate, endDate);
        List<ShiftAssignment> rawAssignments = shiftAssignmentRepository.findByShiftDateBetween(startDate, endDate);

        Map<UUID, List<AssignedUserDto>> assignmentsByShift = rawAssignments.stream()
                .filter(sa -> sa.getShift() != null && sa.getEmployee() != null)
                .collect(Collectors.groupingBy(
                        sa -> sa.getShift().getId(),
                        Collectors.mapping(sa -> {
                            try {
                                UUID currentEmpId = sa.getEmployee().getId();
                                LocalTime startA = sa.getStartTime().toLocalTime();
                                LocalTime endA = sa.getEndTime().toLocalTime();
                                LocalDate dateA = sa.getShift().getShiftDate();

                                boolean hasCollision = rawAssignments.stream()
                                        .filter(other -> !other.getId().equals(sa.getId()))
                                        .filter(other -> other.getEmployee() != null && other.getEmployee().getId().equals(currentEmpId))
                                        .filter(other -> other.getShift() != null && other.getShift().getShiftDate().equals(dateA))
                                        .anyMatch(other -> {
                                            LocalTime startB = other.getStartTime().toLocalTime();
                                            LocalTime endB = other.getEndTime().toLocalTime();
                                            long tolerance = 30;
                                            return startA.isBefore(endB.minusMinutes(tolerance)) &&
                                                    startB.isBefore(endA.minusMinutes(tolerance));
                                        });

                                return AssignedUserDto.builder()
                                        .userId(currentEmpId)
                                        .name((sa.getEmployee().getFirstName() != null ? sa.getEmployee().getFirstName() : "") + " " +
                                                (sa.getEmployee().getLastName() != null ? sa.getEmployee().getLastName() : ""))
                                        .isCollision(hasCollision)
                                        .build();
                            } catch (EntityNotFoundException e) {
                                return AssignedUserDto.builder()
                                        .userId(null)
                                        .name("Smazaný uživatel")
                                        .isCollision(false)
                                        .build();
                            }
                        }, Collectors.toList())
                ));

        List<ScheduleShiftDto> shifts = rawShifts.stream()
                .filter(s -> {
                    try { return s.getStation() != null && s.getStation().getName() != null; }
                    catch (EntityNotFoundException e) { return false; }
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

    /**
     * FÁZE 2: Implementace stránkování u seznamu dostupných uživatelů.
     * Metoda nyní přijímá Pageable a vrací Page<PlannerUserDto>, což je nezbytné pro výkon.
     */
    @GetMapping("/available-users")
    public ResponseEntity<Page<PlannerUserDto>> getAvailableUsersForWeek(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable) {

        List<com.companyapp.backend.entity.Availability> availabilities = availabilityRepository.findByDateRange(startDate, endDate);
        List<UUID> userIdsWhoHaveAvailability = availabilities.stream()
                .map(com.companyapp.backend.entity.Availability::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // Volání UserRepository, který byl v předchozím kroku upraven na Page<User>
        Page<User> activeUsersPage = userRepository.findAllActiveUsersWithDetails(pageable);

        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<ShiftAssignment> monthAssignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(
                activeUsersPage.getContent().stream().map(User::getId).collect(Collectors.toList()),
                monthStart, monthEnd);

        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneId.of("UTC"));

        Page<PlannerUserDto> resultPage = activeUsersPage.map(user -> {
            // Logika filtrování: Pouze uživatelé s nahlášenou dostupností v tomto týdnu
            if (!userIdsWhoHaveAvailability.contains(user.getId())) {
                return null;
            }

            List<Integer> qualifiedIds = user.getQualifiedStations().stream()
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
                if (sa.getEmployee() != null && sa.getEmployee().getId().equals(user.getId()) && sa.getEndTime() != null) {
                    if (sa.getEndTime().isBefore(now)) completed++;
                    else planned++;
                }
            }

            return PlannerUserDto.builder()
                    .userId(user.getId())
                    .name(user.getFirstName() + " " + user.getLastName())
                    .qualifiedStationIds(qualifiedIds)
                    .weekAvailability(weekAvail)
                    .plannedShiftsThisMonth(planned)
                    .completedShiftsThisMonth(completed)
                    .build();
        });

        return ResponseEntity.ok(resultPage);
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