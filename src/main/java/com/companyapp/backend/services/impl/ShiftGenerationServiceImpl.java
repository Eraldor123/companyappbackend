package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftTemplate;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.services.OperatingHoursService;
import com.companyapp.backend.services.ShiftGenerationService;
import com.companyapp.backend.services.dto.request.CreateCustomShiftRequestDto;
import com.companyapp.backend.services.dto.response.DailyHoursDto;
import com.companyapp.backend.services.dto.response.ShiftDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private final OperatingHoursService operatingHoursService;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final StationRepository stationRepository;

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
            // 2. SCÉNÁŘ: Pevně dané časy v šabloně
            else {
                if (template.getStartTime() != null && template.getEndTime() != null) {
                    newShifts.add(createShiftObj(template, currentDate, template.getStartTime(), template.getEndTime()));
                }
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

    private Shift createShiftObj(ShiftTemplate template, LocalDate date, LocalTime start, LocalTime end) {
        Shift shift = new Shift();
        shift.setStation(template.getStation());
        shift.setTemplate(template);
        shift.setShiftDate(date);

        ZonedDateTime startZoned = date.atTime(start).atZone(ZoneId.of("UTC"));
        ZonedDateTime endZoned = date.atTime(end).atZone(ZoneId.of("UTC"));

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

    @Override
    @Transactional
    public void clearWeekSchedule(LocalDate startDate, LocalDate endDate) {
        log.warn("Mažu všechny směny a přiřazení od {} do {}", startDate, endDate);

        var assignments = shiftAssignmentRepository.findByShiftDateBetween(startDate, endDate);
        shiftAssignmentRepository.deleteAll(assignments);

        var shifts = shiftRepository.findByShiftDateBetween(startDate, endDate);
        shiftRepository.deleteAll(shifts);

        log.info("Smazáno {} přiřazení a {} směn.", assignments.size(), shifts.size());
    }

    @Override
    @Transactional
    public void generateCustomShifts(CreateCustomShiftRequestDto request) {
        log.info("Generuji vlastní směnu pro stanoviště {} od {} do {}", request.getStationId(), request.getStartDate(), request.getEndDate());

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno."));

        List<Shift> newShifts = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        while (!currentDate.isAfter(request.getEndDate())) {

            // SCÉNÁŘ 1: Řídí se podle otevírací doby (dopo/odpo)
            if (Boolean.TRUE.equals(request.getUseOpeningHours())) {
                DailyHoursDto dailyHours = (DailyHoursDto) operatingHoursService.getOperatingHoursForDate(currentDate);

                if (Boolean.TRUE.equals(request.getHasDopo()) && dailyHours.getDopoStart() != null && dailyHours.getDopoEnd() != null) {
                    // OPRAVA: Přidán parametr request.getDescription()
                    Shift s = createCustomShiftObj(station, currentDate, LocalTime.parse(dailyHours.getDopoStart()), LocalTime.parse(dailyHours.getDopoEnd()), request.getRequiredCapacity(), request.getDescription());
                    newShifts.add(s);
                }
                if (Boolean.TRUE.equals(request.getHasOdpo()) && dailyHours.getOdpoStart() != null && dailyHours.getOdpoEnd() != null) {
                    // OPRAVA: Přidán parametr request.getDescription()
                    Shift s = createCustomShiftObj(station, currentDate, LocalTime.parse(dailyHours.getOdpoStart()), LocalTime.parse(dailyHours.getOdpoEnd()), request.getRequiredCapacity(), request.getDescription());
                    newShifts.add(s);
                }
            }
            // SCÉNÁŘ 2: Máme zadán přesný čas natvrdo (původní chování)
            else if (request.getStartTime() != null && request.getEndTime() != null) {
                LocalTime start = LocalTime.parse(request.getStartTime());
                LocalTime end = LocalTime.parse(request.getEndTime());
                // OPRAVA: Přidán parametr request.getDescription()
                Shift s = createCustomShiftObj(station, currentDate, start, end, request.getRequiredCapacity(), request.getDescription());
                newShifts.add(s);
            }

            currentDate = currentDate.plusDays(1);
        }

        if (newShifts.isEmpty()) {
            log.warn("Nebyly vygenerovány žádné vlastní směny. Zkontrolujte zadání.");
        } else {
            shiftRepository.saveAll(newShifts);
            log.info("Vygenerováno {} vlastních směn.", newShifts.size());
        }
    }

    // Pomocná metoda pro vlastní směny (aby to bylo čisté)
    private Shift createCustomShiftObj(Station station, LocalDate date, LocalTime start, LocalTime end, Integer capacity, String description) {
        Shift shift = new Shift();
        shift.setStation(station);
        shift.setTemplate(null);
        shift.setShiftDate(date);
        shift.setDescription(description); // <--- TADY SE ULOŽÍ POPISEK

        ZonedDateTime startZoned = date.atTime(start).atZone(ZoneId.of("UTC"));
        ZonedDateTime endZoned = date.atTime(end).atZone(ZoneId.of("UTC"));

        if (endZoned.isBefore(startZoned)) {
            endZoned = endZoned.plusDays(1);
        }

        shift.setStartTime(startZoned);
        shift.setEndTime(endZoned);
        shift.setRequiredCapacity(capacity);
        return shift;
    }
}