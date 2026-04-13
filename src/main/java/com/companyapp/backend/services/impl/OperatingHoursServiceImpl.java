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
import com.companyapp.backend.services.dto.response.OperatingHoursDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperatingHoursServiceImpl implements OperatingHoursService {

    private final StandardOperatingHoursRepository standardHoursRepo;
    private final SeasonalRegimeRepository seasonalRepo;
    private final PauseRuleRepository pauseRuleRepo;

    @Override
    @Transactional(readOnly = true)
    public Object getOperatingHoursForDate(LocalDate date) {
        // 1. Zkusíme najít sezónu
        List<SeasonalRegime> seasons = seasonalRepo.findActiveRegimesForDate(date);

        if (!seasons.isEmpty()) {
            SeasonalRegime season = seasons.get(0);
            return DailyHoursDto.builder()
                    .date(date)
                    // Pokud v sezóně nějaký čas chybí, dosadíme bezpečnou záchranu
                    .dopoStart(season.getDopoStart() != null ? season.getDopoStart().toString() : "08:00")
                    .dopoEnd(season.getDopoEnd() != null ? season.getDopoEnd().toString() : "12:00")
                    .odpoStart(season.getOdpoStart() != null ? season.getOdpoStart().toString() : "12:30")
                    .odpoEnd(season.getOdpoEnd() != null ? season.getOdpoEnd().toString() : "16:00")
                    .isSeasonal(true)
                    .build();
        }

        // 2. Pokud není sezóna, bereme standardní dobu (pokud neexistuje, vytvoříme prázdnou s defaulty)
        StandardOperatingHours std = standardHoursRepo.findAll().stream().findFirst().orElse(new StandardOperatingHours());
        boolean isWeekend = (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);

        // 3. Víkend s odlišnou otevíračkou
        if (isWeekend && !Boolean.TRUE.equals(std.getWeekendSame())) {
            return DailyHoursDto.builder()
                    .date(date)
                    .dopoStart(std.getWeekendDopoStart() != null ? std.getWeekendDopoStart().toString() : "08:00")
                    .dopoEnd(std.getWeekendDopoEnd() != null ? std.getWeekendDopoEnd().toString() : "12:00")
                    .odpoStart(std.getWeekendOdpoStart() != null ? std.getWeekendOdpoStart().toString() : "12:30")
                    .odpoEnd(std.getWeekendOdpoEnd() != null ? std.getWeekendOdpoEnd().toString() : "16:00")
                    .isSeasonal(false)
                    .build();
        }
        // 4. Víkend shodný s týdnem NEBO všední den
        else {
            return DailyHoursDto.builder()
                    .date(date)
                    .dopoStart(std.getWeekDopoStart() != null ? std.getWeekDopoStart().toString() : "08:00")
                    .dopoEnd(std.getWeekDopoEnd() != null ? std.getWeekDopoEnd().toString() : "12:00")
                    .odpoStart(std.getWeekOdpoStart() != null ? std.getWeekOdpoStart().toString() : "12:30")
                    .odpoEnd(std.getWeekOdpoEnd() != null ? std.getWeekOdpoEnd().toString() : "16:00")
                    .isSeasonal(false)
                    .build();
        }
    }

    // =====================================
    // STANDARDNÍ OTEVÍRACÍ DOBA
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public StandardHoursDto getStandardHours() {
        // Sáhneme si prostě pro první záznam z databáze
        StandardOperatingHours entity = standardHoursRepo.findAll().stream().findFirst().orElse(new StandardOperatingHours());
        StandardHoursDto dto = new StandardHoursDto();
        dto.setWeekDopoStart(entity.getWeekDopoStart() != null ? entity.getWeekDopoStart().toString() : "08:00");
        dto.setWeekDopoEnd(entity.getWeekDopoEnd() != null ? entity.getWeekDopoEnd().toString() : "12:00");
        dto.setWeekOdpoStart(entity.getWeekOdpoStart() != null ? entity.getWeekOdpoStart().toString() : "12:30");
        dto.setWeekOdpoEnd(entity.getWeekOdpoEnd() != null ? entity.getWeekOdpoEnd().toString() : "16:00");
        dto.setWeekendSame(entity.getWeekendSame() != null ? entity.getWeekendSame() : false);
        dto.setWeekendDopoStart(entity.getWeekendDopoStart() != null ? entity.getWeekendDopoStart().toString() : "08:00");
        dto.setWeekendDopoEnd(entity.getWeekendDopoEnd() != null ? entity.getWeekendDopoEnd().toString() : "12:00");
        dto.setWeekendOdpoStart(entity.getWeekendOdpoStart() != null ? entity.getWeekendOdpoStart().toString() : "12:30");
        dto.setWeekendOdpoEnd(entity.getWeekendOdpoEnd() != null ? entity.getWeekendOdpoEnd().toString() : "16:00");
        return dto;
    }

    @Override
    @Transactional
    public void updateStandardHours(StandardHoursDto dto) {
        // Zde je oprava - vytahujeme existující první záznam a nevnucujeme mu natvrdo ID=1
        StandardOperatingHours entity = standardHoursRepo.findAll().stream().findFirst().orElse(new StandardOperatingHours());

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
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveSeason(SeasonalRegimeDto dto) {
        SeasonalRegime entity = dto.getId() != null ? seasonalRepo.findById(dto.getId()).orElse(new SeasonalRegime()) : new SeasonalRegime();
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
        // Obdobná oprava i pro pravidlo pauzy
        PauseRule entity = pauseRuleRepo.findAll().stream().findFirst().orElse(new PauseRule());

        entity.setTriggerHours(dto.getTriggerHours());
        entity.setPauseDurationMinutes(dto.getPauseMinutes());

        pauseRuleRepo.save(entity);
    }
    @Override
    public boolean isAnyOperatingHoursPresent() {
        return standardHoursRepo.count() > 0;
    }
}