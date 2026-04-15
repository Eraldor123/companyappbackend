package com.companyapp.backend.services.impl;

import com.companyapp.backend.services.TimeCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCalculationServiceImpl implements TimeCalculationService {

    // V reálné aplikaci bychom toto četli z PauseRuleRepository, zde pro ukázku jako konstanty
    private static final long PAUSE_THRESHOLD_HOURS = 6;
    private static final long PAUSE_DURATION_MINUTES = 30;

    @Override
    public Duration calculateNetWorkTime(ZonedDateTime startTime, ZonedDateTime endTime) {
        // OPRAVA java:S2178: Změněno z | na || pro zajištění short-circuit logiky a ochrany před NPE
        if (startTime == null || endTime == null || startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Neplatný časový úsek směny.");
        }

        Duration grossTime = Duration.between(startTime, endTime);

        // Pokud pracovník odpracoval 6 hodin a více, strhneme mu 30 minut
        if (grossTime.toHours() >= PAUSE_THRESHOLD_HOURS) {
            Duration netTime = grossTime.minusMinutes(PAUSE_DURATION_MINUTES);
            log.debug("Aplikována pauza 30 minut. Hrubý čas: {}, Čistý čas: {}", grossTime, netTime);
            return netTime;
        }

        return grossTime;
    }
}