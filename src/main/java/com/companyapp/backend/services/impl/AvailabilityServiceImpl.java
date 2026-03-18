package com.companyapp.backend.services.impl;

import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.services.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    @Override
    @Transactional
    public void submitMonthlyAvailability(UUID userId, YearMonth month, List<LocalDate> availableDays) {
        // 1. Smazat původní dostupnost (Drafty) pro daný měsíc a uživatele
        // 2. Pro každý den v 'availableDays' založit novou entitu Availability se stavem SUBMITTED
        // 3. Uložit vše do databáze (availabilityRepository.saveAll)
    }
}