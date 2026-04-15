package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.*;
import com.companyapp.backend.repository.*;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.AutoPlanService;
import com.companyapp.backend.services.ShiftAssignmentService;
import com.companyapp.backend.services.dto.request.AutoPlanRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
    private final AuditLogService auditLogService;

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
        List<ShiftAssignment> allAssignmentsInPeriod = shiftAssignmentRepository.findByShiftDateBetween(start, end);

        Map<UUID, Long> userShiftCounts = allAssignmentsInPeriod.stream()
                .collect(Collectors.groupingBy(sa -> sa.getEmployee().getId(), Collectors.counting()));

        shifts.sort(Comparator.comparing(Shift::getStartTime));

        int successfulAssignments = 0;

        for (Shift shift : shifts) {
            int currentAssigned = shift.getAssignments().size();
            int slotsToFill = shift.getRequiredCapacity() - currentAssigned;

            for (int i = 0; i < slotsToFill; i++) {
                User best = findBestCandidate(shift, users, avails, allAssignmentsInPeriod, userShiftCounts, request);
                if (best != null) {
                    try {
                        shiftAssignmentService.assignShift(shift.getId(), best.getId());
                        successfulAssignments++;
                        userShiftCounts.merge(best.getId(), 1L, Long::sum);
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
                                   AutoPlanRequestDto req) {
        User best = null;
        double maxScore = -1.0;

        for (User user : users) {
            // OPRAVA java:S135: Sloučení kontrol do jedné podmínky místo více příkazů 'continue'
            boolean available = isUserAvailable(user, shift, avails);
            boolean hasOverlap = allAssignments.stream()
                    .filter(sa -> sa.getEmployee().getId().equals(user.getId()))
                    .anyMatch(sa -> isOverlapping(sa, shift));

            if (available && !hasOverlap) {
                // Výpočet skóre proběhne jen pro validní kandidáty
                double totalScore = calculateScore(user, shift, userShiftCounts, req);

                if (totalScore > maxScore) {
                    maxScore = totalScore;
                    best = user;
                }
            }
        }
        return best;
    }

    /**
     * Pomocná metoda pro výpočet skóre - oddělení logiky zvyšuje čitelnost.
     */
    private double calculateScore(User user, Shift shift, Map<UUID, Long> userShiftCounts, AutoPlanRequestDto req) {
        boolean isQualified = user.getQualifiedStations().stream()
                .anyMatch(s -> s.getId().equals(shift.getStation().getId()));

        double trainingScore = isQualified ? (100.0 - req.getTrainingWeight()) : (double) req.getTrainingWeight();

        long totalShifts = userShiftCounts.getOrDefault(user.getId(), 0L);
        double fairnessScore = (20.0 - totalShifts) * (req.getFairnessWeight() / 10.0);

        double morningBonus = (shift.getStartTime().withZoneSameInstant(ZoneId.of("Europe/Prague")).getHour() < 12) ? 50.0 : 0.0;

        return trainingScore + fairnessScore + morningBonus;
    }

    private boolean isOverlapping(ShiftAssignment sa, Shift shift) {
        LocalDateTime startA = sa.getStartTime();
        LocalDateTime endA = sa.getEndTime();
        LocalDateTime startB = shift.getStartTime().toLocalDateTime();
        LocalDateTime endB = shift.getEndTime().toLocalDateTime();

        return startA.isBefore(endB.minusMinutes(1)) && startB.isBefore(endA.minusMinutes(1));
    }

    private boolean isUserAvailable(User user, Shift shift, List<Availability> avails) {
        return avails.stream()
                .filter(a -> a.getUserId().equals(user.getId()) && a.getAvailableDate().equals(shift.getShiftDate()))
                .anyMatch(a -> {
                    ZonedDateTime localStart = shift.getStartTime().withZoneSameInstant(ZoneId.of("Europe/Prague"));
                    return localStart.getHour() < 12 ? a.isMorning() : a.isAfternoon();
                });
    }
}