package com.companyapp.backend.services.impl;

import com.companyapp.backend.services.OperatingHoursService;
import com.companyapp.backend.services.dto.response.OperatingHoursDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperatingHoursServiceImpl implements OperatingHoursService {

    @Override
    public Object getOperatingHoursForDate(LocalDate date) {
        log.debug("Zjišťuji provozní hodiny pro datum: {}", date);

        // PROTOŽE ZATÍM NEMÁME ENTITY PRO OTEVÍRACÍ DOBU (StandardHours atd.),
        // Vracíme na pevno nastavený objekt, aby frontend nepadal.
        // Až vytvoříš v databázi tabulku pro nastavení podniku, zde to načteš.

        return OperatingHoursDto.builder()
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(20, 0))
                .isSeasonalRegime(false)
                .regimeName("Běžný provoz")
                .build();
    }
}