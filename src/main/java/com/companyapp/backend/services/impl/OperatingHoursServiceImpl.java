package com.companyapp.backend.services.impl;

import com.companyapp.backend.services.OperatingHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperatingHoursServiceImpl implements OperatingHoursService {

    // Zde by byly injektovány repozitáře pro SeasonalRegimeRepository a StandardHoursRepository

    @Override
    @Transactional(readOnly = true)
    public Object getOperatingHoursForDate(LocalDate date) {
        log.debug("Zjišťuji provozní hodiny pro datum: {}", date);

        // 1. Nejprve zkontrolujeme, zda pro daný den neexistuje Sezónní režim (např. Halloween)
        // Optional<SeasonalRegime> regime = seasonalRegimeRepository.findActiveRegimeForDate(date);

        // 2. Pokud existuje, vrátíme upravené časy (např. prodloužená otevírací doba do 23:00)
        // if (regime.isPresent()) {
        //     return mapRegimeToDto(regime.get());
        // }

        // 3. Pokud neexistuje, zjistíme, zda je všední den (Týden) nebo Víkend
        // boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY | date.getDayOfWeek() == DayOfWeek.SUNDAY;

        // 4. Vrátíme standardní otevírací dobu
        // return getStandardHours(isWeekend);

        return null; // Zástupná návratová hodnota
    }
}