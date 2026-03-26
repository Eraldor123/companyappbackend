package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.PauseRuleDto;
import com.companyapp.backend.services.dto.request.SeasonalRegimeDto;
import com.companyapp.backend.services.dto.request.StandardHoursDto;

import java.time.LocalDate;
import java.util.List;

public interface OperatingHoursService {
    Object getOperatingHoursForDate(LocalDate date); // Tuhle přepíšeme ve Fázi 4

    // Nové metody pro React UI
    StandardHoursDto getStandardHours();
    void updateStandardHours(StandardHoursDto dto);

    List<SeasonalRegimeDto> getAllSeasons();
    void saveSeason(SeasonalRegimeDto dto);
    void deleteSeason(Integer id);

    PauseRuleDto getPauseRule();
    void updatePauseRule(PauseRuleDto dto);
}