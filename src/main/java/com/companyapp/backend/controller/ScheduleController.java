package com.companyapp.backend.controller;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.AutoPlanService;
import com.companyapp.backend.services.OperatingHoursService;
import com.companyapp.backend.services.dto.request.AutoPlanRequestDto;
import com.companyapp.backend.services.dto.response.AssignedUserDto;
import com.companyapp.backend.services.dto.response.PlannerUserDto;
import com.companyapp.backend.services.dto.response.ScheduleShiftDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
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
    public ResponseEntity<Map<String, Object>> getWeeklySchedule(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (!operatingHoursService.isAnyOperatingHoursPresent()) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY)
                    .body(Map.of("message", "V systému není nastavena otevírací doba. Kalendář nelze zobrazit."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("days", generateDaysInfo(startDate, endDate));

        List<Shift> rawShifts = shiftRepository.findByShiftDateBetween(startDate, endDate);
        List<ShiftAssignment> rawAssignments = shiftAssignmentRepository.findByShiftDateBetween(startDate, endDate);

        Map<UUID, List<AssignedUserDto>> assignmentsByShift = groupAssignmentsByShift(rawAssignments);
        List<ScheduleShiftDto> shifts = mapToScheduleShiftDtos(rawShifts, assignmentsByShift);

        response.put("shifts", shifts);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-users")
    public ResponseEntity<List<PlannerUserDto>> getAvailableUsersForWeek(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<com.companyapp.backend.entity.Availability> availabilities = availabilityRepository.findByDateRange(startDate, endDate);
        List<UUID> userIdsWithAvail = availabilities.stream()
                .map(com.companyapp.backend.entity.Availability::getUserId)
                .distinct()
                .toList();

        // OPRAVA 1: Použijeme velkou "stránku" natvrdo, abychom z databáze vytáhli VŠECHNY uživatele
        // Tím se vyhneme tomu, že se zkontroluje jen prvních 20 lidí.
        Page<User> activeUsersPage = userRepository.findAllActiveUsersWithDetails(org.springframework.data.domain.PageRequest.of(0, 1000));

        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<UUID> userIds = activeUsersPage.getContent().stream().map(User::getId).toList();
        List<ShiftAssignment> monthAssignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(userIds, monthStart, monthEnd);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

        // OPRAVA 2: Vrátíme čistý List<PlannerUserDto> a rovnou odstraníme neplatné (null) uživatele
        List<PlannerUserDto> resultList = activeUsersPage.getContent().stream()
                .map(user -> mapToPlannerUserDto(user, userIdsWithAvail, availabilities, monthAssignments, now))
                .filter(u -> u != null) // Zbavíme se těch, co pro daný týden nemají dostupnost
                .toList();

        return ResponseEntity.ok(resultList);
    }

    // --- REFAKTORIZOVANÉ METODY PRO MINIMÁLNÍ KOGNITIVNÍ SLOŽITOST ---

    private PlannerUserDto mapToPlannerUserDto(User user, List<UUID> userIdsWithAvail,
                                               List<com.companyapp.backend.entity.Availability> availabilities,
                                               List<ShiftAssignment> monthAssignments, LocalDateTime now) {
        if (!userIdsWithAvail.contains(user.getId())) return null;

        Map<String, String> weekAvail = extractUserAvailability(user.getId(), availabilities);

        // Zploštění statistiky pomocí streamu (odstraňuje vnořený if uvnitř for)
        Map<Boolean, Long> stats = monthAssignments.stream()
                .filter(sa -> isAssignmentForUser(sa, user.getId()))
                .collect(Collectors.partitioningBy(sa -> sa.getEndTime().isBefore(now), Collectors.counting()));

        return PlannerUserDto.builder()
                .userId(user.getId())
                .name(user.getFirstName() + " " + user.getLastName())
                .qualifiedStationIds(user.getQualifiedStations().stream().map(com.companyapp.backend.entity.Station::getId).toList())
                .weekAvailability(weekAvail)
                .plannedShiftsThisMonth(stats.get(false).intValue())
                .completedShiftsThisMonth(stats.get(true).intValue())
                .build();
    }

    private Map<String, String> extractUserAvailability(UUID userId, List<com.companyapp.backend.entity.Availability> allAvailabilities) {
        Map<String, String> userAvail = new HashMap<>();
        allAvailabilities.stream()
                .filter(a -> a.getUserId().equals(userId))
                .forEach(a -> userAvail.put(a.getAvailableDate().toString(), getAvailabilityLabel(a)));
        return userAvail;
    }

    private String getAvailabilityLabel(com.companyapp.backend.entity.Availability a) {
        if (a.isMorning() && a.isAfternoon()) return "CELÝ DEN";
        return a.isMorning() ? "DOP" : "ODP";
    }

    private boolean isAssignmentForUser(ShiftAssignment sa, UUID userId) {
        return sa.getEmployee() != null && sa.getEmployee().getId().equals(userId) && sa.getEndTime() != null;
    }

    private List<Object> generateDaysInfo(LocalDate startDate, LocalDate endDate) {
        List<Object> days = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            days.add(operatingHoursService.getOperatingHoursForDate(current));
            current = current.plusDays(1);
        }
        return days;
    }

    private Map<UUID, List<AssignedUserDto>> groupAssignmentsByShift(List<ShiftAssignment> rawAssignments) {
        return rawAssignments.stream()
                .filter(sa -> sa.getShift() != null && sa.getEmployee() != null)
                .collect(Collectors.groupingBy(
                        sa -> sa.getShift().getId(),
                        Collectors.mapping(sa -> buildAssignedUserDto(sa, rawAssignments), Collectors.toList())
                ));
    }

    private AssignedUserDto buildAssignedUserDto(ShiftAssignment sa, List<ShiftAssignment> allAssignments) {
        try {
            return createAssignedUserDto(sa, allAssignments);
        } catch (EntityNotFoundException e) {
            return AssignedUserDto.builder().userId(null).name("Smazaný uživatel").isCollision(false).build();
        }
    }

    private AssignedUserDto createAssignedUserDto(ShiftAssignment sa, List<ShiftAssignment> allAssignments) {
        UUID empId = sa.getEmployee().getId();
        LocalTime start = sa.getStartTime().toLocalTime();
        LocalTime end = sa.getEndTime().toLocalTime();
        LocalDate date = sa.getShift().getShiftDate();

        boolean hasCollision = allAssignments.stream()
                .filter(other -> !other.getId().equals(sa.getId()))
                .filter(other -> other.getEmployee() != null && other.getEmployee().getId().equals(empId))
                .filter(other -> other.getShift() != null && other.getShift().getShiftDate().equals(date))
                .anyMatch(other -> isTimeOverlapping(start, end, other.getStartTime().toLocalTime(), other.getEndTime().toLocalTime()));

        return AssignedUserDto.builder()
                .userId(empId)
                .name(sa.getEmployee().getFirstName() + " " + sa.getEmployee().getLastName())
                .isCollision(hasCollision)
                .build();
    }

    private boolean isTimeOverlapping(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        long tolerance = 30;
        return startA.isBefore(endB.minusMinutes(tolerance)) && startB.isBefore(endA.minusMinutes(tolerance));
    }

    private List<ScheduleShiftDto> mapToScheduleShiftDtos(List<Shift> rawShifts, Map<UUID, List<AssignedUserDto>> assignmentsByShift) {
        return rawShifts.stream()
                .filter(s -> s.getStation() != null && s.getStation().getName() != null)
                .map(s -> buildScheduleShiftDto(s, assignmentsByShift.getOrDefault(s.getId(), new ArrayList<>())))
                .toList();
    }

    private ScheduleShiftDto buildScheduleShiftDto(Shift s, List<AssignedUserDto> assigned) {
        return ScheduleShiftDto.builder()
                .id(s.getId())
                .stationId(s.getStation().getId())
                .templateId(s.getTemplate() != null ? s.getTemplate().getId() : null)
                .shiftDate(s.getShiftDate())
                .startTime(s.getStartTime() != null ? s.getStartTime().toString() : "00:00")
                .endTime(s.getEndTime() != null ? s.getEndTime().toString() : "00:00")
                .requiredCapacity(s.getRequiredCapacity() != null ? s.getRequiredCapacity() : 1)
                .description(s.getDescription() != null ? s.getDescription() : "")
                .assignedUsers(assigned)
                .build();
    }

    @PostMapping("/auto-plan")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> runAutoPlan(@RequestBody AutoPlanRequestDto request) {
        if (!operatingHoursService.isAnyOperatingHoursPresent()) {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).build();
        }
        autoPlanService.runAutoPlanning(request);
        return ResponseEntity.ok().build();
    }
}