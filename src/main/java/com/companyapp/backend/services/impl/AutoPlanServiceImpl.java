package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.*;
import com.companyapp.backend.repository.*;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.AutoPlanService;
import com.companyapp.backend.services.ShiftAssignmentService;
import com.companyapp.backend.services.OperatingHoursService; // PŘIDÁNO
import com.companyapp.backend.services.dto.request.AutoPlanRequestDto;
import com.companyapp.backend.services.dto.request.PauseRuleDto; // PŘIDÁNO
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final AuditLogService auditLogService;
    private final OperatingHoursService operatingHoursService; // PŘIDÁNO: Pro přístup k pravidlům pauz

    @Override
    @Transactional
    public void runAutoPlanning(AutoPlanRequestDto request) {
        LocalDate start = (request.getTargetDate() != null) ? request.getTargetDate() : request.getStartDate();
        LocalDate end = (request.getTargetDate() != null) ? request.getTargetDate() : request.getEndDate();

        if (start == null) start = LocalDate.now();
        if (end == null) end = start.plusDays(6);

        log.info("Spouštím auto-plánování pro období: {} až {}", start, end);

        List<Shift> shifts = shiftRepository.findByShiftDateBetween(start, end);

        if (request.getCategoryId() != null) {
            shifts = shifts.stream()
                    .filter(s -> s.getStation().getCategory().getId().equals(request.getCategoryId()))
                    .toList();
        }

        List<User> users = userRepository.findAllActiveUsersWithDetails(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        List<Availability> avails = availabilityRepository.findByDateRange(start, end);
        List<ShiftAssignment> allAssignmentsInPeriod = new ArrayList<>(shiftAssignmentRepository.findByShiftDateBetween(start, end));

        Map<UUID, Long> userShiftCounts = allAssignmentsInPeriod.stream()
                .collect(Collectors.groupingBy(sa -> sa.getEmployee().getId(), Collectors.counting()));

        shifts.sort(Comparator.comparing(Shift::getStartTime));

        // NOVÉ: Načtení povoleného překryvu z modulu Pravidla pauz
        int allowedOverlapMinutes = getDynamicOverlapLimit();
        log.info("AutoPlan používá limit překryvu: {} min (načteno z nastavení areálu)", allowedOverlapMinutes);

        int successfulAssignments = 0;

        for (Shift shift : shifts) {
            int currentAssigned = shift.getAssignments().size();
            int slotsToFill = shift.getRequiredCapacity() - currentAssigned;

            for (int i = 0; i < slotsToFill; i++) {
                User best = findBestCandidate(shift, users, avails, allAssignmentsInPeriod, userShiftCounts, request, allowedOverlapMinutes);
                if (best != null) {
                    try {
                        shiftAssignmentService.assignShift(shift.getId(), best.getId());
                        successfulAssignments++;
                        userShiftCounts.merge(best.getId(), 1L, Long::sum);

                        ShiftAssignment tempAssignment = new ShiftAssignment();
                        tempAssignment.setShift(shift);
                        tempAssignment.setEmployee(best);
                        tempAssignment.setStartTime(shift.getStartTime().toLocalDateTime());
                        tempAssignment.setEndTime(shift.getEndTime().toLocalDateTime());
                        allAssignmentsInPeriod.add(tempAssignment);

                    } catch (Exception e) {
                        log.warn("Přiřazení selhalo pro {}: {}", best.getLastName(), e.getMessage());
                    }
                }
            }
        }

        auditLogService.logAction(
                "RUN_AUTOPLAN",
                "ShiftAssignment",
                "Range_" + start + "_to_" + end,
                "AutoPlán dokončen. Obsazeno " + successfulAssignments + " míst."
        );
    }

    private User findBestCandidate(Shift shift, List<User> users, List<Availability> avails,
                                   List<ShiftAssignment> allAssignments, Map<UUID, Long> userShiftCounts,
                                   AutoPlanRequestDto req, int overlapLimit) {
        User best = null;
        double maxScore = -1.0;

        for (User user : users) {
            boolean isAlreadyOnThisShift = shift.getAssignments().stream()
                    .anyMatch(a -> a.getEmployee().getId().equals(user.getId()));

            boolean isAlreadyOnThisShiftLocal = allAssignments.stream()
                    .anyMatch(sa -> sa.getShift().getId().equals(shift.getId()) && sa.getEmployee().getId().equals(user.getId()));

            if (isAlreadyOnThisShift || isAlreadyOnThisShiftLocal) {
                continue;
            }

            boolean available = isUserAvailable(user, shift, avails);

            // NOVÉ: Předání dynamického limitu do metody pro kontrolu překryvu
            boolean hasOverlap = allAssignments.stream()
                    .filter(sa -> sa.getEmployee().getId().equals(user.getId()))
                    .anyMatch(sa -> isOverlapping(sa, shift, overlapLimit));

            if (available && !hasOverlap) {
                double totalScore = calculateScore(user, shift, userShiftCounts, req);

                if (totalScore > maxScore) {
                    maxScore = totalScore;
                    best = user;
                }
            }
        }
        return best;
    }

    private double calculateScore(User user, Shift shift, Map<UUID, Long> userShiftCounts, AutoPlanRequestDto req) {
        boolean isQualified = user.getQualifiedStations().stream()
                .anyMatch(s -> s.getId().equals(shift.getStation().getId()));

        double trainingScore = isQualified ? (100.0 - req.getTrainingWeight()) : (double) req.getTrainingWeight();

        long totalShifts = userShiftCounts.getOrDefault(user.getId(), 0L);
        double fairnessScore = (20.0 - totalShifts) * (req.getFairnessWeight() / 10.0);

        double morningBonus = (shift.getStartTime().withZoneSameInstant(ZoneId.of("Europe/Prague")).getHour() < 12) ? 50.0 : 0.0;

        return trainingScore + fairnessScore + morningBonus;
    }

    private boolean isOverlapping(ShiftAssignment sa, Shift shift, int limitMinutes) {
        LocalDateTime startA = sa.getStartTime();
        LocalDateTime endA = sa.getEndTime();
        LocalDateTime startB = shift.getStartTime().toLocalDateTime();
        LocalDateTime endB = shift.getEndTime().toLocalDateTime();

        if (!startA.isBefore(endB) || !startB.isBefore(endA)) {
            return false;
        }

        LocalDateTime overlapStart = startA.isAfter(startB) ? startA : startB;
        LocalDateTime overlapEnd = endA.isBefore(endB) ? endA : endB;

        long overlapMinutes = Duration.between(overlapStart, overlapEnd).toMinutes();

        // Dynamické vyhodnocení podle načteného limitu (např. 30 min)
        return overlapMinutes > limitMinutes;
    }

    private boolean isUserAvailable(User user, Shift shift, List<Availability> avails) {
        return avails.stream()
                .filter(a -> a.getUserId().equals(user.getId()) && a.getAvailableDate().equals(shift.getShiftDate()))
                .anyMatch(a -> {
                    return shift.getStartTime().getHour() < 12 ? a.isMorning() : a.isAfternoon();
                });
    }

    /**
     * POMOCNÁ METODA: Načte limit pauzy z OperatingHoursService.
     */
    private int getDynamicOverlapLimit() {
        try {
            PauseRuleDto rule = operatingHoursService.getPauseRule();
            return (rule != null && rule.getPauseMinutes() != null) ? rule.getPauseMinutes() : 30;
        } catch (Exception e) {
            log.error("Nepodařilo se načíst dynamická pravidla pauz: {}", e.getMessage());
            return 30; // Bezpečný fallback
        }
    }
}