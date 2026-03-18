package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.enums.AvailabilityStatus;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void submitMonthlyAvailability(UUID userId, YearMonth month, List<LocalDate> availableDays) {
        log.info("Zpracovávám dostupnost pro uživatele {} na měsíc {}", userId, month);

        // 1. Najdeme uživatele
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nenalezen."));

        // 2. Ideálně bychom zde měli smazat staré "drafty" pro tento měsíc,
        // ale pro jednoduchost teď pouze proiterujeme odeslané dny a uložíme je.
        // (V reálné aplikaci bys přidal metodu do repozitáře pro smazání starých záznamů daného měsíce)

        List<Availability> availabilitiesToSave = new ArrayList<>();

        for (LocalDate date : availableDays) {
            // Bezpečnostní pojistka: Ujistíme se, že zaslaný den opravdu patří do daného měsíce
            if (YearMonth.from(date).equals(month)) {

                // Zkontrolujeme, zda už pro tento den nemá záznam, abychom neporušili unikátní omezení v DB
                boolean exists = availabilityRepository.existsByUserIdAndAvailableDateAndStatus(
                        userId, date, AvailabilityStatus.AVAILABLE);

                if (!exists) {
                    Availability availability = new Availability();
                    availability.setUser(user);
                    availability.setAvailableDate(date);
                    availability.setStatus(AvailabilityStatus.AVAILABLE);

                    // Frontend nám zatím neposílá morning/afternoon v poli date, tak je nastavíme defaultně na true
                    availability.setMorning(true);
                    availability.setAfternoon(true);

                    availabilitiesToSave.add(availability);
                }
            }
        }

        // 3. Hromadné uložení do databáze
        availabilityRepository.saveAll(availabilitiesToSave);
        log.info("Uloženo {} nových záznamů dostupnosti.", availabilitiesToSave.size());
    }
}