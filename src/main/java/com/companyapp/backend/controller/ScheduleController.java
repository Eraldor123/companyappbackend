package com.companyapp.backend.controller;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.OperatingHoursService;
import com.companyapp.backend.services.AutoPlanService; // Přidat import
import com.companyapp.backend.services.dto.request.AutoPlanRequestDto;
import com.companyapp.backend.services.dto.response.AssignedUserDto;
import com.companyapp.backend.services.dto.response.PlannerUserDto;
import com.companyapp.backend.services.dto.response.ScheduleShiftDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
public class ScheduleController {

    private final OperatingHoursService operatingHoursService;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AutoPlanService autoPlanService;
    @GetMapping("/week-view")
    public ResponseEntity<Map<String, Object>> getWeeklySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> response = new HashMap<>();

        // 1. Hlavičky dnů s otevírací dobou
        List<Object> days = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            days.add(operatingHoursService.getOperatingHoursForDate(current));
            current = current.plusDays(1);
        }
        response.put("days", days);

        // 2. Načtení všech směn a přiřazení pro daný týden
        List<Shift> rawShifts = shiftRepository.findByShiftDateBetween(startDate, endDate);
        List<ShiftAssignment> rawAssignments = shiftAssignmentRepository.findByShiftDateBetween(startDate, endDate);

        // 3. Rozřazení lidí do jejich směn (rychlé seskupení podle Shift ID)
        Map<java.util.UUID, List<AssignedUserDto>> assignmentsByShift = rawAssignments.stream()
                .collect(Collectors.groupingBy(
                        sa -> sa.getShift().getId(),
                        Collectors.mapping(sa -> AssignedUserDto.builder()
                                .userId(sa.getEmployee().getId())
                                .name(sa.getEmployee().getFirstName() + " " + sa.getEmployee().getLastName())
                                .build(), Collectors.toList())
                ));

        // 4. Sestavení finálních DTO pro kalendář
        List<ScheduleShiftDto> shifts = rawShifts.stream().map(s -> ScheduleShiftDto.builder()
                .id(s.getId())
                .stationId(s.getStation().getId())
                .templateId(s.getTemplate() != null ? s.getTemplate().getId() : null)
                .shiftDate(s.getShiftDate())
                .startTime(s.getStartTime().toString())
                .endTime(s.getEndTime().toString())
                .requiredCapacity(s.getRequiredCapacity())
                .description(s.getDescription()) // <--- PŘIDÁNO: Tady se propisuje popisek na frontend!
                .assignedUsers(assignmentsByShift.getOrDefault(s.getId(), new ArrayList<>()))
                .build()
        ).collect(Collectors.toList());

        response.put("shifts", shifts);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-users")
    public ResponseEntity<List<PlannerUserDto>> getAvailableUsersForWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // 1. Najdeme všechny, kdo si v tomto týdnu zadali, že mají čas
        List<com.companyapp.backend.entity.Availability> availabilities = availabilityRepository.findByDateRange(startDate, endDate);

        // 2. Unikátní ID těchto uživatelů
        List<java.util.UUID> userIds = availabilities.stream()
                .map(com.companyapp.backend.entity.Availability::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>()); // Nikdo nemá čas :(
        }

        // 3. Načteme tyto uživatele i s jejich kvalifikacemi
        List<com.companyapp.backend.entity.User> activeUsers = userRepository.findAllActiveUsersWithDetails().stream()
                .filter(u -> userIds.contains(u.getId()))
                .collect(Collectors.toList());

        // 4. Výpočet měsíčních statistik (vezmeme měsíc podle startDate)
        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<ShiftAssignment> monthAssignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(userIds, monthStart, monthEnd);
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneId.of("UTC"));

        List<PlannerUserDto> result = new ArrayList<>();

        // 5. Poskládáme to do DTO pro každý profil
        for (com.companyapp.backend.entity.User user : activeUsers) {

            // a) Zjistíme kvalifikace
            List<Integer> qualifiedIds = user.getQualifiedStations().stream()
                    .map(com.companyapp.backend.entity.Station::getId)
                    .collect(Collectors.toList());

            // b) Zmapujeme dny, kdy má čas
            Map<String, String> weekAvail = new HashMap<>();
            availabilities.stream().filter(a -> a.getUserId().equals(user.getId())).forEach(a -> {
                String type = (a.isMorning() && a.isAfternoon()) ? "CELÝ DEN" : (a.isMorning() ? "DOP" : "ODP");
                weekAvail.put(a.getAvailableDate().toString(), type);
            });

            // c) Spočítáme směny
            int planned = 0;
            int completed = 0;
            for (ShiftAssignment sa : monthAssignments) {
                if (sa.getEmployee().getId().equals(user.getId())) {
                    if (sa.getEndTime().isBefore(now)) {
                        completed++; // Už má po směně
                    } else {
                        planned++; // Směna ho teprve čeká
                    }
                }
            }

            result.add(PlannerUserDto.builder()
                    .userId(user.getId())
                    .name(user.getFirstName() + " " + user.getLastName())
                    .qualifiedStationIds(qualifiedIds)
                    .weekAvailability(weekAvail)
                    .plannedShiftsThisMonth(planned)
                    .completedShiftsThisMonth(completed)
                    .build());
        }

        return ResponseEntity.ok(result);
    }
    @PostMapping("/auto-plan")
    public ResponseEntity<?> runAutoPlan(@RequestBody AutoPlanRequestDto request) { // Upravit na AutoPlanRequestDto
        autoPlanService.runAutoPlanning(request); // Volat autoPlanService místo scheduleService
        return ResponseEntity.ok().build();
    }
}