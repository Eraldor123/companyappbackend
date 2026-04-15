package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.entity.ShiftTemplate;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.services.AuditLogService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftGenerationServiceImpl implements ShiftGenerationService {

    private final ShiftRepository shiftRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final OperatingHoursService operatingHoursService;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final StationRepository stationRepository;
    private final AuditLogService auditLogService;

    private static final String ENTITY_NAME = "Shift";
    private static final String STATION_NOT_FOUND = "Stanoviště nenalezeno.";
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Override
    @Transactional
    public List<ShiftDto> generateShiftsFromTemplate(LocalDate startDate, LocalDate endDate, Integer templateId) {
        log.info("Generuji směny ze šablony {} od {} do {}", templateId, startDate, endDate);

        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Šablona nenalezena."));

        List<Shift> newShifts = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            newShifts.addAll(processDailyTemplate(currentDate, template));
            currentDate = currentDate.plusDays(1);
        }

        if (newShifts.isEmpty()) {
            log.warn("Nebyly vygenerovány žádné směny. Zkontrolujte časy v šabloně.");
        } else {
            shiftRepository.saveAll(newShifts);
            auditLogService.logAction(
                    "GENERATE_SHIFTS_TEMPLATE",
                    ENTITY_NAME,
                    "Template_" + templateId,
                    "Hromadně vygenerováno " + newShifts.size() + " směn ze šablony '" + template.getName() + "'."
            );
        }

        return newShifts.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional
    public void generateCustomShifts(CreateCustomShiftRequestDto request) {
        log.info("Generuji vlastní směny pro stanoviště {} od {} do {}", request.getStationId(), request.getStartDate(), request.getEndDate());

        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException(STATION_NOT_FOUND));

        List<Shift> newShifts = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        while (!currentDate.isAfter(request.getEndDate())) {
            newShifts.addAll(processDailyCustom(currentDate, request, station));
            currentDate = currentDate.plusDays(1);
        }

        if (!newShifts.isEmpty()) {
            shiftRepository.saveAll(newShifts);
            auditLogService.logAction("GENERATE_CUSTOM_SHIFTS", ENTITY_NAME, "Custom_" + station.getName(), "Vygenerováno " + newShifts.size() + " vlastních směn.");
        }
    }

    // =====================================
    // REFAKTOROVANÉ POMOCNÉ METODY (Snížení složitosti)
    // =====================================

    private List<Shift> processDailyTemplate(LocalDate date, ShiftTemplate template) {
        List<Shift> dailyShifts = new ArrayList<>();

        if (Boolean.TRUE.equals(template.getUseOpeningHours())) {
            DailyHoursDto hours = (DailyHoursDto) operatingHoursService.getOperatingHoursForDate(date);
            if (Boolean.TRUE.equals(template.getHasDopo()) && hours.getDopoStart() != null) {
                dailyShifts.add(createShiftObj(template, date, LocalTime.parse(hours.getDopoStart()), LocalTime.parse(hours.getDopoEnd())));
            }
            if (Boolean.TRUE.equals(template.getHasOdpo()) && hours.getOdpoStart() != null) {
                dailyShifts.add(createShiftObj(template, date, LocalTime.parse(hours.getOdpoStart()), LocalTime.parse(hours.getOdpoEnd())));
            }
        } else {
            if (template.getStartTime() != null) {
                dailyShifts.add(createShiftObj(template, date, template.getStartTime(), template.getEndTime()));
            }
            if (template.getStartTime2() != null) {
                dailyShifts.add(createShiftObj(template, date, template.getStartTime2(), template.getEndTime2()));
            }
        }
        return dailyShifts;
    }

    private List<Shift> processDailyCustom(LocalDate date, CreateCustomShiftRequestDto req, Station station) {
        List<Shift> dailyShifts = new ArrayList<>();

        if (Boolean.TRUE.equals(req.getUseOpeningHours())) {
            DailyHoursDto hours = (DailyHoursDto) operatingHoursService.getOperatingHoursForDate(date);
            if (Boolean.TRUE.equals(req.getHasDopo()) && hours.getDopoStart() != null) {
                dailyShifts.add(createCustomShiftObj(station, date, LocalTime.parse(hours.getDopoStart()), LocalTime.parse(hours.getDopoEnd()), req));
            }
            if (Boolean.TRUE.equals(req.getHasOdpo()) && hours.getOdpoStart() != null) {
                dailyShifts.add(createCustomShiftObj(station, date, LocalTime.parse(hours.getOdpoStart()), LocalTime.parse(hours.getOdpoEnd()), req));
            }
        } else if (req.getStartTime() != null) {
            dailyShifts.add(createCustomShiftObj(station, date, LocalTime.parse(req.getStartTime()), LocalTime.parse(req.getEndTime()), req));
        }
        return dailyShifts;
    }

    private Shift createShiftObj(ShiftTemplate template, LocalDate date, LocalTime start, LocalTime end) {
        Shift shift = new Shift();
        shift.setStation(template.getStation());
        shift.setTemplate(template);
        shift.setShiftDate(date);
        shift.setStartTime(atZoned(date, start));
        shift.setEndTime(calculateEndTime(date, start, end));
        shift.setRequiredCapacity(template.getWorkersNeeded());
        return shift;
    }

    private Shift createCustomShiftObj(Station station, LocalDate date, LocalTime start, LocalTime end, CreateCustomShiftRequestDto req) {
        Shift shift = new Shift();
        shift.setStation(station);
        shift.setShiftDate(date);
        shift.setDescription(req.getDescription());
        shift.setStartTime(atZoned(date, start));
        shift.setEndTime(calculateEndTime(date, start, end));
        shift.setRequiredCapacity(req.getRequiredCapacity());
        return shift;
    }

    private ZonedDateTime atZoned(LocalDate date, LocalTime time) {
        return date.atTime(time).atZone(UTC);
    }

    private ZonedDateTime calculateEndTime(LocalDate date, LocalTime start, LocalTime end) {
        ZonedDateTime startZoned = atZoned(date, start);
        ZonedDateTime endZoned = atZoned(date, end);
        return endZoned.isBefore(startZoned) ? endZoned.plusDays(1) : endZoned;
    }

    @Override
    @Transactional
    public void copyWeekSchedule(LocalDate sourceWeekStart, LocalDate targetWeekStart) {
        LocalDate sourceWeekEnd = sourceWeekStart.plusDays(6);
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(sourceWeekStart, targetWeekStart);

        List<Shift> sourceShifts = shiftRepository.findByShiftDateBetween(sourceWeekStart, sourceWeekEnd);
        List<Shift> newShifts = sourceShifts.stream().map(src -> {
            Shift copy = new Shift();
            copy.setStation(src.getStation());
            copy.setTemplate(src.getTemplate());
            copy.setShiftDate(src.getShiftDate().plusDays(daysDiff));
            copy.setStartTime(src.getStartTime().plusDays(daysDiff));
            copy.setEndTime(src.getEndTime().plusDays(daysDiff));
            copy.setRequiredCapacity(src.getRequiredCapacity());
            return copy;
        }).toList();

        shiftRepository.saveAll(newShifts);
        auditLogService.logAction("COPY_WEEK_SCHEDULE", ENTITY_NAME, "Week_" + targetWeekStart, "Zkopírován týden směn.");
    }

    @Override
    @Transactional
    public void clearWeekSchedule(LocalDate startDate, LocalDate endDate) {
        var assignments = shiftAssignmentRepository.findByShiftDateBetween(startDate, endDate);
        shiftAssignmentRepository.deleteAll(assignments);

        var shifts = shiftRepository.findByShiftDateBetween(startDate, endDate);
        int shiftsCount = shifts.size();
        shiftRepository.deleteAll(shifts);

        auditLogService.logAction("CLEAR_WEEK_SCHEDULE", ENTITY_NAME, "Range_" + startDate, "Smazáno " + shiftsCount + " směn.");
    }

    private ShiftDto mapToDto(Shift s) {
        return ShiftDto.builder()
                .id(s.getId())
                .stationId(s.getStation().getId())
                .stationName(s.getStation().getName())
                .templateId(s.getTemplate() != null ? s.getTemplate().getId() : null)
                .templateName(s.getTemplate() != null ? s.getTemplate().getName() : null)
                .shiftDate(s.getShiftDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .requiredCapacity(s.getRequiredCapacity())
                .build();
    }
}