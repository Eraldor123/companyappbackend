package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftTemplate;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.services.ShiftGenerationService;
import com.companyapp.backend.services.dto.response.ShiftDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftGenerationServiceImpl implements ShiftGenerationService {

    private final ShiftRepository shiftRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;

    @Override
    @Transactional
    public List<Object> generateShiftsFromTemplate(LocalDate startDate, LocalDate endDate, UUID templateId) {
        log.info("Generuji směny ze šablony {} od {} do {}", templateId, startDate, endDate);

        // V DTO máme ID jako Integer, ale interface bere UUID. Předpokládám, že šablona má Integer ID (podle tvé entity).
        // Zde ukázka s Integer ID, jelikož ShiftTemplate má @Id Integer.
        Integer parsedTemplateId = Integer.valueOf(templateId.toString()); // Zástupná konverze pro napojení

        ShiftTemplate template = shiftTemplateRepository.findById(parsedTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Šablona nenalezena."));

        List<Shift> newShifts = new ArrayList<>();
        LocalDate currentDate = startDate;

        // Projdeme dny od startDate do endDate (včetně)
        while (!currentDate.isAfter(endDate)) {
            Shift shift = new Shift();
            shift.setStation(template.getStation());
            shift.setTemplate(template);
            shift.setShiftDate(currentDate);

            // Spojení data a času do ZonedDateTime (předpokládáme UTC)
            ZonedDateTime startZoned = currentDate.atTime(template.getStartTime()).atZone(ZoneId.of("UTC"));
            ZonedDateTime endZoned = currentDate.atTime(template.getEndTime()).atZone(ZoneId.of("UTC"));

            // Ošetření noční směny (konec je další den)
            if (endZoned.isBefore(startZoned)) {
                endZoned = endZoned.plusDays(1);
            }

            shift.setStartTime(startZoned);
            shift.setEndTime(endZoned);
            shift.setRequiredCapacity(template.getWorkersNeeded());

            newShifts.add(shift);
            currentDate = currentDate.plusDays(1);
        }

        shiftRepository.saveAll(newShifts);
        log.info("Vygenerováno {} nových směn.", newShifts.size());

        // Zde bys reálně vrátil mapované ShiftDto objekty, např:
        return newShifts.stream().map(s -> (Object) ShiftDto.builder()
                .id(s.getId())
                .stationName(s.getStation().getName())
                .shiftDate(s.getShiftDate())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void copyWeekSchedule(LocalDate sourceWeekStart, LocalDate targetWeekStart) {
        log.info("Kopíruji týden od {} do týdne od {}", sourceWeekStart, targetWeekStart);

        LocalDate sourceWeekEnd = sourceWeekStart.plusDays(6);
        // Spočítáme rozdíl dní (obvykle 7, 14 atd.)
        long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(sourceWeekStart, targetWeekStart);

        // 1. Najdeme všechny směny v daném zdrojovém týdnu
        List<Shift> sourceShifts = shiftRepository.findByShiftDateBetween(sourceWeekStart, sourceWeekEnd);
        List<Shift> newShifts = new ArrayList<>();

        // 2. Pro každou směnu vytvoříme kopii s novým datem
        for (Shift src : sourceShifts) {
            Shift copy = new Shift();
            copy.setStation(src.getStation());
            copy.setTemplate(src.getTemplate());
            copy.setShiftDate(src.getShiftDate().plusDays(daysDifference));

            // Posuneme i přesné časy Start/End
            copy.setStartTime(src.getStartTime().plusDays(daysDifference));
            copy.setEndTime(src.getEndTime().plusDays(daysDifference));
            copy.setRequiredCapacity(src.getRequiredCapacity());

            newShifts.add(copy);
        }

        // 3. Hromadné uložení
        shiftRepository.saveAll(newShifts);
        log.info("Úspěšně zkopírováno {} směn do nového týdne.", newShifts.size());
    }
}