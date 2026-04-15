package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.PauseRuleDto;
import com.companyapp.backend.services.dto.request.SeasonalRegimeDto;
import com.companyapp.backend.services.dto.request.StandardHoursDto;

import java.time.LocalDate;
import java.util.List;

public interface OperatingHoursService {

    /**
     * Získá provozní dobu pro konkrétní datum (zohledňuje standard i sezóny).
     * Ponecháno pro Fázi 4 (výpočetní logika).
     */
    Object getOperatingHoursForDate(LocalDate date);

    // ==========================================
    // Metody pro React UI (Správa provozní doby)
    // ==========================================

    StandardHoursDto getStandardHours();
    void updateStandardHours(StandardHoursDto dto);

    List<SeasonalRegimeDto> getAllSeasons();
    void saveSeason(SeasonalRegimeDto dto);
    void deleteSeason(Integer id);

    PauseRuleDto getPauseRule();
    void updatePauseRule(PauseRuleDto dto);

    /**
     * OPRAVA: Metoda zjišťuje, zda je systém nastaven.
     * I když je aktuálně volána inverzně (s vykřičníkem), ponecháváme pozitivní název
     * pro čistotu API a budoucí logiku validací.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isAnyOperatingHoursPresent();
}