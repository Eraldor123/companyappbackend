package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.*;
import com.companyapp.backend.repository.*;
import com.companyapp.backend.services.AuditLogService; // PŘIDÁNO
import com.companyapp.backend.services.AutoPlanService;
import com.companyapp.backend.services.ShiftAssignmentService;
import com.companyapp.backend.services.dto.request.AutoPlanRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPlanServiceImpl implements AutoPlanService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftAssignmentService shiftAssignmentService;
    private final AuditLogService auditLogService; // PŘIDÁNO

    @Override
    @Transactional
    public void runAutoPlanning(AutoPlanRequestDto request) {
        LocalDate start = (request.getTargetDate() != null) ? request.getTargetDate() : request.getStartDate();
        LocalDate end = (request.getTargetDate() != null) ? request.getTargetDate() : request.getEndDate();

        if (start == null) start = LocalDate.now();
        if (end == null) end = start.plusDays(6);

        log.info("Spouštím auto-plánování pro období: {} až {} (Férovost: {}%, Zaučování: {}%)",
                start, end, request.getFairnessWeight(), request.getTrainingWeight());

        List<Shift> shifts = shiftRepository.findByShiftDateBetween(start, end);

        if (request.getCategoryId() != null) {
            shifts = shifts.stream()
                    .filter(s -> s.getStation().getCategory().getId().equals(request.getCategoryId()))
                    .collect(Collectors.toList());
            log.info("Aplikován filtr na kategorii ID {}. K plánování zbývá {} směn.", request.getCategoryId(), shifts.size());
        }

        List<User> users = userRepository.findAllActiveUsersWithDetails();
        List<Availability> avails = availabilityRepository.findByDateRange(start, end);

        shifts.sort(Comparator.comparing(Shift::getStartTime));

        int successfulAssignments = 0; // Sledujeme počet úspěšných přiřazení pro log

        for (Shift shift : shifts) {
            long currentAssigned = shiftAssignmentRepository.countByShiftId(shift.getId());
            int slotsToFill = shift.getRequiredCapacity() - (int) currentAssigned;

            for (int i = 0; i < slotsToFill; i++) {
                User best = findBestCandidate(shift, users, avails, request);
                if (best != null) {
                    try {
                        shiftAssignmentService.assignShift(shift.getId(), best.getId());
                        successfulAssignments++;
                        log.info("Automaticky přiřazen {} na stanoviště {}", best.getLastName(), shift.getStation().getName());
                    } catch (Exception e) {
                        log.warn("Přiřazení selhalo pro {}: {}", best.getLastName(), e.getMessage());
                    }
                }
            }
        }

        // ZÁZNAM DO AUDITU
        auditLogService.logAction(
                "RUN_AUTOPLAN",
                "ShiftAssignment",
                "Range_" + start + "_to_" + end,
                "Spuštěn AutoPlán. Výsledek: Úspěšně automaticky obsazeno " + successfulAssignments + " míst ve směnách v období od " + start + " do " + end + "."
        );
    }

    private User findBestCandidate(Shift shift, List<User> users, List<Availability> avails, AutoPlanRequestDto req) {
        User best = null;
        double maxScore = -1.0;

        for (User user : users) {
            if (!isUserAvailable(user, shift, avails)) continue;

            LocalDateTime checkStart = shift.getStartTime().toLocalDateTime().plusMinutes(31);
            LocalDateTime checkEnd = shift.getEndTime().toLocalDateTime().minusMinutes(31);

            if (checkStart.isAfter(checkEnd)) {
                checkStart = shift.getStartTime().toLocalDateTime().plusMinutes(1);
                checkEnd = shift.getEndTime().toLocalDateTime().minusMinutes(1);
            }

            long overlapCount = shiftAssignmentRepository.countOverlappingShifts(
                    user.getId(),
                    checkStart,
                    checkEnd
            );

            if (overlapCount > 0) continue;

            boolean isQualified = user.getQualifiedStations().stream()
                    .anyMatch(s -> s.getId().equals(shift.getStation().getId()));

            double trainingScore = isQualified ? (100.0 - req.getTrainingWeight()) : (double) req.getTrainingWeight();

            long weeklyShifts = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(
                    List.of(user.getId()),
                    shift.getShiftDate().with(java.util.Calendar.MONDAY == 1 ? java.time.DayOfWeek.MONDAY : java.time.DayOfWeek.MONDAY),
                    shift.getShiftDate().with(java.time.DayOfWeek.SUNDAY)
            ).size();
            double fairnessScore = (20.0 - weeklyShifts) * (req.getFairnessWeight() / 10.0);

            double morningBonus = (shift.getStartTime().withZoneSameInstant(ZoneId.of("Europe/Prague")).getHour() < 12) ? 50.0 : 0.0;

            double totalScore = trainingScore + fairnessScore + morningBonus;

            if (totalScore > maxScore) {
                maxScore = totalScore;
                best = user;
            }
        }
        return best;
    }

    private boolean isUserAvailable(User user, Shift shift, List<Availability> avails) {
        return avails.stream()
                .filter(a -> a.getUserId().equals(user.getId()) && a.getAvailableDate().equals(shift.getShiftDate()))
                .anyMatch(a -> {
                    ZonedDateTime localStart = shift.getStartTime().withZoneSameInstant(ZoneId.of("Europe/Prague"));
                    int hour = localStart.getHour();

                    if (hour < 12) {
                        return a.isMorning();
                    } else {
                        return a.isAfternoon();
                    }
                });
    }
}