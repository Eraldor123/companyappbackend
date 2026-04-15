package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.PauseRule;
import com.companyapp.backend.entity.SeasonalRegime;
import com.companyapp.backend.entity.StandardOperatingHours;
import com.companyapp.backend.repository.PauseRuleRepository;
import com.companyapp.backend.repository.SeasonalRegimeRepository;
import com.companyapp.backend.repository.StandardOperatingHoursRepository;
import com.companyapp.backend.services.OperatingHoursService;
import com.companyapp.backend.services.dto.request.PauseRuleDto;
import com.companyapp.backend.services.dto.request.SeasonalRegimeDto;
import com.companyapp.backend.services.dto.request.StandardHoursDto;
import com.companyapp.backend.services.dto.response.DailyHoursDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperatingHoursServiceImpl implements OperatingHoursService {

    private final StandardOperatingHoursRepository standardHoursRepo;
    private final SeasonalRegimeRepository seasonalRepo;
    private final PauseRuleRepository pauseRuleRepo;

    // KONSTANTY PRO ODSTRANĚNÍ DUPLIKACÍ (java:S1192)
    private static final String DEFAULT_DOPO_START = "08:00";
    private static final String DEFAULT_DOPO_END = "12:00";
    private static final String DEFAULT_ODPO_START = "12:30";
    private static final String DEFAULT_ODPO_END = "16:00";

    @Override
    @Transactional(readOnly = true)
    public DailyHoursDto getOperatingHoursForDate(LocalDate date) {
        // SNÍŽENÍ SLOŽITOSTI: Rozdělení na sezónní a standardní logiku
        List<SeasonalRegime> seasons = seasonalRepo.findActiveRegimesForDate(date);

        if (!seasons.isEmpty()) {
            return buildSeasonalDailyDto(date, seasons.getFirst());
        }

        return buildStandardDailyDto(date);
    }

    // =====================================
    // STANDARDNÍ OTEVÍRACÍ DOBA
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public StandardHoursDto getStandardHours() {
        StandardOperatingHours entity = getFirstOrEmptyStandard();
        StandardHoursDto dto = new StandardHoursDto();

        dto.setWeekDopoStart(formatTime(entity.getWeekDopoStart(), DEFAULT_DOPO_START));
        dto.setWeekDopoEnd(formatTime(entity.getWeekDopoEnd(), DEFAULT_DOPO_END));
        dto.setWeekOdpoStart(formatTime(entity.getWeekOdpoStart(), DEFAULT_ODPO_START));
        dto.setWeekOdpoEnd(formatTime(entity.getWeekOdpoEnd(), DEFAULT_ODPO_END));

        // OPRAVA: Odstranění unnecessary boolean literal
        dto.setWeekendSame(Boolean.TRUE.equals(entity.getWeekendSame()));

        dto.setWeekendDopoStart(formatTime(entity.getWeekendDopoStart(), DEFAULT_DOPO_START));
        dto.setWeekendDopoEnd(formatTime(entity.getWeekendDopoEnd(), DEFAULT_DOPO_END));
        dto.setWeekendOdpoStart(formatTime(entity.getWeekendOdpoStart(), DEFAULT_ODPO_START));
        dto.setWeekendOdpoEnd(formatTime(entity.getWeekendOdpoEnd(), DEFAULT_ODPO_END));

        return dto;
    }

    @Override
    @Transactional
    public void updateStandardHours(StandardHoursDto dto) {
        StandardOperatingHours entity = getFirstOrEmptyStandard();

        entity.setWeekDopoStart(LocalTime.parse(dto.getWeekDopoStart()));
        entity.setWeekDopoEnd(LocalTime.parse(dto.getWeekDopoEnd()));
        entity.setWeekOdpoStart(LocalTime.parse(dto.getWeekOdpoStart()));
        entity.setWeekOdpoEnd(LocalTime.parse(dto.getWeekOdpoEnd()));
        entity.setWeekendSame(dto.getWeekendSame());
        entity.setWeekendDopoStart(LocalTime.parse(dto.getWeekendDopoStart()));
        entity.setWeekendDopoEnd(LocalTime.parse(dto.getWeekendDopoEnd()));
        entity.setWeekendOdpoStart(LocalTime.parse(dto.getWeekendOdpoStart()));
        entity.setWeekendOdpoEnd(LocalTime.parse(dto.getWeekendOdpoEnd()));

        standardHoursRepo.save(entity);
    }

    // =====================================
    // SEZÓNNÍ REŽIMY
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public List<SeasonalRegimeDto> getAllSeasons() {
        // OPRAVA: Stream.toList() místo collect(Collectors.toList())
        return seasonalRepo.findAll().stream().map(s -> {
            SeasonalRegimeDto dto = new SeasonalRegimeDto();
            dto.setId(s.getId());
            dto.setName(s.getName());
            dto.setStartDate(s.getStartDate().toString());
            dto.setEndDate(s.getEndDate().toString());
            dto.setDopoStart(s.getDopoStart().toString());
            dto.setDopoEnd(s.getDopoEnd().toString());
            dto.setOdpoStart(s.getOdpoStart().toString());
            dto.setOdpoEnd(s.getOdpoEnd().toString());
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public void saveSeason(SeasonalRegimeDto dto) {
        SeasonalRegime entity = (dto.getId() != null)
                ? seasonalRepo.findById(dto.getId()).orElse(new SeasonalRegime())
                : new SeasonalRegime();

        entity.setName(dto.getName());
        entity.setStartDate(LocalDate.parse(dto.getStartDate()));
        entity.setEndDate(LocalDate.parse(dto.getEndDate()));
        entity.setDopoStart(LocalTime.parse(dto.getDopoStart()));
        entity.setDopoEnd(LocalTime.parse(dto.getDopoEnd()));
        entity.setOdpoStart(LocalTime.parse(dto.getOdpoStart()));
        entity.setOdpoEnd(LocalTime.parse(dto.getOdpoEnd()));
        seasonalRepo.save(entity);
    }

    @Override
    @Transactional
    public void deleteSeason(Integer id) {
        seasonalRepo.deleteById(id);
    }

    // =====================================
    // PRAVIDLA PAUZ
    // =====================================

    @Override
    @Transactional(readOnly = true)
    public PauseRuleDto getPauseRule() {
        PauseRule entity = pauseRuleRepo.findAll().stream().findFirst().orElse(new PauseRule());
        PauseRuleDto dto = new PauseRuleDto();
        dto.setTriggerHours(entity.getTriggerHours() != null ? entity.getTriggerHours() : BigDecimal.valueOf(6.0));
        dto.setPauseMinutes(entity.getPauseDurationMinutes() != null ? entity.getPauseDurationMinutes() : 30);
        return dto;
    }

    @Override
    @Transactional
    public void updatePauseRule(PauseRuleDto dto) {
        PauseRule entity = pauseRuleRepo.findAll().stream().findFirst().orElse(new PauseRule());
        entity.setTriggerHours(dto.getTriggerHours());
        entity.setPauseDurationMinutes(dto.getPauseMinutes());
        pauseRuleRepo.save(entity);
    }

    @Override
    public boolean isAnyOperatingHoursPresent() {
        return standardHoursRepo.count() > 0;
    }

    // =====================================
    // SOUKROMÉ POMOCNÉ METODY (REFAKTOR)
    // =====================================

    private DailyHoursDto buildSeasonalDailyDto(LocalDate date, SeasonalRegime season) {
        return DailyHoursDto.builder()
                .date(date)
                .dopoStart(formatTime(season.getDopoStart(), DEFAULT_DOPO_START))
                .dopoEnd(formatTime(season.getDopoEnd(), DEFAULT_DOPO_END))
                .odpoStart(formatTime(season.getOdpoStart(), DEFAULT_ODPO_START))
                .odpoEnd(formatTime(season.getOdpoEnd(), DEFAULT_ODPO_END))
                .isSeasonal(true)
                .build();
    }

    private DailyHoursDto buildStandardDailyDto(LocalDate date) {
        StandardOperatingHours std = getFirstOrEmptyStandard();
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        boolean useWeekend = isWeekend && !Boolean.TRUE.equals(std.getWeekendSame());

        return DailyHoursDto.builder()
                .date(date)
                .dopoStart(useWeekend ? formatTime(std.getWeekendDopoStart(), DEFAULT_DOPO_START) : formatTime(std.getWeekDopoStart(), DEFAULT_DOPO_START))
                .dopoEnd(useWeekend ? formatTime(std.getWeekendDopoEnd(), DEFAULT_DOPO_END) : formatTime(std.getWeekDopoEnd(), DEFAULT_DOPO_END))
                .odpoStart(useWeekend ? formatTime(std.getWeekendOdpoStart(), DEFAULT_ODPO_START) : formatTime(std.getWeekOdpoStart(), DEFAULT_ODPO_START))
                .odpoEnd(useWeekend ? formatTime(std.getWeekendOdpoEnd(), DEFAULT_ODPO_END) : formatTime(std.getWeekOdpoEnd(), DEFAULT_ODPO_END))
                .isSeasonal(false)
                .build();
    }

    private String formatTime(LocalTime time, String defaultVal) {
        return time != null ? time.toString() : defaultVal;
    }

    private StandardOperatingHours getFirstOrEmptyStandard() {
        return standardHoursRepo.findAll().stream().findFirst().orElse(new StandardOperatingHours());
    }
}