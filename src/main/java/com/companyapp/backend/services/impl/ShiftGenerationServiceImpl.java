package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftTemplate;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.services.OperatingHoursService; // NOVÉ
import com.companyapp.backend.services.ShiftGenerationService;
import com.companyapp.backend.services.dto.response.DailyHoursDto; // NOVÉ
import com.companyapp.backend.services.dto.response.ShiftDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime; // NOVÉ
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftGenerationServiceImpl implements ShiftGenerationService {

    private final ShiftRepository shiftRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final OperatingHoursService operatingHoursService; // PŘIDÁNO: Abychom znali otevírací dobu areálu

    @Override
    @Transactional
    public List<Object> generateShiftsFromTemplate(LocalDate startDate, LocalDate endDate, Integer templateId) {
        log.info("Generuji směny ze šablony {} od {} do {}", templateId, startDate, endDate);

        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Šablona nenalezena."));

        List<Shift> newShifts = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {

            // 1. SCÉNÁŘ: Šablona se řídí otevírací dobou areálu
            if (Boolean.TRUE.equals(template.getUseOpeningHours())) {
                DailyHoursDto dailyHours = (DailyHoursDto) operatingHoursService.getOperatingHoursForDate(currentDate);

                // Má dopolední část?
                if (Boolean.TRUE.equals(template.getHasDopo()) && dailyHours.getDopoStart() != null && dailyHours.getDopoEnd() != null) {
                    newShifts.add(createShiftObj(template, currentDate, LocalTime.parse(dailyHours.getDopoStart()), LocalTime.parse(dailyHours.getDopoEnd())));
                }
                // Má odpolední část?
                if (Boolean.TRUE.equals(template.getHasOdpo()) && dailyHours.getOdpoStart() != null && dailyHours.getOdpoEnd() != null) {
                    newShifts.add(createShiftObj(template, currentDate, LocalTime.parse(dailyHours.getOdpoStart()), LocalTime.parse(dailyHours.getOdpoEnd())));
                }
            }
            // 2. SCÉNÁŘ: Pevně dané časy v šabloně (klasická nebo dělená)
            else {
                if (template.getStartTime() != null && template.getEndTime() != null) {
                    newShifts.add(createShiftObj(template, currentDate, template.getStartTime(), template.getEndTime()));
                }
                // Dělená směna (odpolední část - např. 14:00 - 18:00)
                if (template.getStartTime2() != null && template.getEndTime2() != null) {
                    newShifts.add(createShiftObj(template, currentDate, template.getStartTime2(), template.getEndTime2()));
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        if (newShifts.isEmpty()) {
            log.warn("Nebyly vygenerovány žádné směny. Zkontrolujte časy v šabloně nebo nastavení provozu areálu.");
        } else {
            shiftRepository.saveAll(newShifts);
            log.info("Vygenerováno {} nových směn.", newShifts.size());
        }

        return newShifts.stream().map(s -> (Object) ShiftDto.builder()
                .id(s.getId())
                .stationName(s.getStation().getName())
                .shiftDate(s.getShiftDate())
                .build()).collect(Collectors.toList());
    }

    // Pomocná metoda, aby byl kód čistý a neopakovali jsme to samé dokola
    private Shift createShiftObj(ShiftTemplate template, LocalDate date, LocalTime start, LocalTime end) {
        Shift shift = new Shift();
        shift.setStation(template.getStation());
        shift.setTemplate(template);
        shift.setShiftDate(date);

        ZonedDateTime startZoned = date.atTime(start).atZone(ZoneId.of("UTC"));
        ZonedDateTime endZoned = date.atTime(end).atZone(ZoneId.of("UTC"));

        // Ošetření noční směny (konec je až další den ráno)
        if (endZoned.isBefore(startZoned)) {
            endZoned = endZoned.plusDays(1);
        }

        shift.setStartTime(startZoned);
        shift.setEndTime(endZoned);
        shift.setRequiredCapacity(template.getWorkersNeeded());
        return shift;
    }

    @Override
    @Transactional
    public void copyWeekSchedule(LocalDate sourceWeekStart, LocalDate targetWeekStart) {
        log.info("Kopíruji týden od {} do týdne od {}", sourceWeekStart, targetWeekStart);

        LocalDate sourceWeekEnd = sourceWeekStart.plusDays(6);
        long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(sourceWeekStart, targetWeekStart);

        List<Shift> sourceShifts = shiftRepository.findByShiftDateBetween(sourceWeekStart, sourceWeekEnd);
        List<Shift> newShifts = new ArrayList<>();

        for (Shift src : sourceShifts) {
            Shift copy = new Shift();
            copy.setStation(src.getStation());
            copy.setTemplate(src.getTemplate());
            copy.setShiftDate(src.getShiftDate().plusDays(daysDifference));

            copy.setStartTime(src.getStartTime().plusDays(daysDifference));
            copy.setEndTime(src.getEndTime().plusDays(daysDifference));
            copy.setRequiredCapacity(src.getRequiredCapacity());

            newShifts.add(copy);
        }

        shiftRepository.saveAll(newShifts);
        log.info("Úspěšně zkopírováno {} směn do nového týdne.", newShifts.size());
    }

    // 1. Přidej nahoru k ostatním repozitářům (pod shiftTemplateRepository)
    private final com.companyapp.backend.repository.ShiftAssignmentRepository shiftAssignmentRepository;

    // 2. Přidej samotnou metodu kamkoliv do třídy:
    @Override
    @Transactional
    public void clearWeekSchedule(LocalDate startDate, LocalDate endDate) {
        log.warn("Mažu všechny směny a přiřazení od {} do {}", startDate, endDate);

        // Nejprve musíme smazat přiřazení lidí na směny (kvůli vazbám v DB)
        var assignments = shiftAssignmentRepository.findByShiftDateBetween(startDate, endDate);
        shiftAssignmentRepository.deleteAll(assignments);

        // Pak smažeme samotné směny
        var shifts = shiftRepository.findByShiftDateBetween(startDate, endDate);
        shiftRepository.deleteAll(shifts);

        log.info("Smazáno {} přiřazení a {} směn.", assignments.size(), shifts.size());
    }
}