package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.*;
import com.companyapp.backend.repository.*;
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

    @Override
    @Transactional
    public void runAutoPlanning(AutoPlanRequestDto request) {
        // Určení rozsahu: buď konkrétní den, nebo rozsah týdne z frontendu
        LocalDate start = (request.getTargetDate() != null) ? request.getTargetDate() : request.getStartDate();
        LocalDate end = (request.getTargetDate() != null) ? request.getTargetDate() : request.getEndDate();

        if (start == null) start = LocalDate.now();
        if (end == null) end = start.plusDays(6);

        log.info("Spouštím auto-plánování pro období: {} až {} (Férovost: {}%, Zaučování: {}%)",
                start, end, request.getFairnessWeight(), request.getTrainingWeight());

        List<Shift> shifts = shiftRepository.findByShiftDateBetween(start, end);

        // <--- NOVINKA: VYFILTRUJEME JEN ZVOLENOU KATEGORII --->
        if (request.getCategoryId() != null) {
            shifts = shifts.stream()
                    .filter(s -> s.getStation().getCategory().getId().equals(request.getCategoryId()))
                    .collect(Collectors.toList());
            log.info("Aplikován filtr na kategorii ID {}. K plánování zbývá {} směn.", request.getCategoryId(), shifts.size());
        }

        List<User> users = userRepository.findAllActiveUsersWithDetails();
        List<Availability> avails = availabilityRepository.findByDateRange(start, end);

        // KLÍČOVÉ: Seřadíme směny chronologicky, aby se nejdříve plnilo ráno
        shifts.sort(Comparator.comparing(Shift::getStartTime));

        for (Shift shift : shifts) {
            long currentAssigned = shiftAssignmentRepository.countByShiftId(shift.getId());
            int slotsToFill = shift.getRequiredCapacity() - (int) currentAssigned;

            for (int i = 0; i < slotsToFill; i++) {
                User best = findBestCandidate(shift, users, avails, request);
                if (best != null) {
                    try {
                        // assignShift má vlastní validace, ale díky naší toleranci v findBestCandidate projde
                        shiftAssignmentService.assignShift(shift.getId(), best.getId());
                        log.info("Automaticky přiřazen {} na stanoviště {}", best.getLastName(), shift.getStation().getName());
                    } catch (Exception e) {
                        log.warn("Přiřazení selhalo pro {}: {}", best.getLastName(), e.getMessage());
                    }
                }
            }
        }
    }

    private User findBestCandidate(Shift shift, List<User> users, List<Availability> avails, AutoPlanRequestDto req) {
        User best = null;
        double maxScore = -1.0;

        for (User user : users) {
            // 1. KONTROLA DOSTUPNOSTI (DOP/ODP)
            if (!isUserAvailable(user, shift, avails)) continue;

            // 2. KONTROLA PŘEKRYVU S TOLERANCÍ (31 MINUT)
            // Ignorujeme okraje směny, aby mohl stejný člověk dělat ranní (do 14:00) i odpolední (od 13:30)
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

            // 3. VÝPOČET SKÓRE
            // a) Kvalifikace (Zaučování)
            boolean isQualified = user.getQualifiedStations().stream()
                    .anyMatch(s -> s.getId().equals(shift.getStation().getId()));

            double trainingScore = isQualified ? (100.0 - req.getTrainingWeight()) : (double) req.getTrainingWeight();

            // b) Férovost (Aby jeden člověk neměl všechno, pokud je víc lidí)
            long weeklyShifts = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(
                    List.of(user.getId()),
                    shift.getShiftDate().with(java.util.Calendar.MONDAY == 1 ? java.time.DayOfWeek.MONDAY : java.time.DayOfWeek.MONDAY),
                    shift.getShiftDate().with(java.time.DayOfWeek.SUNDAY)
            ).size();
            double fairnessScore = (20.0 - weeklyShifts) * (req.getFairnessWeight() / 10.0);

            // c) Priorita pro dopoledne (Morning Bonus)
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
                    // Převod času směny na pražský čas pro správné vyhodnocení DOP/ODP
                    ZonedDateTime localStart = shift.getStartTime().withZoneSameInstant(ZoneId.of("Europe/Prague"));
                    int hour = localStart.getHour();

                    if (hour < 12) {
                        return a.isMorning(); // Směna začíná dopoledne
                    } else {
                        return a.isAfternoon(); // Směna začíná odpoledne
                    }
                });
    }
}