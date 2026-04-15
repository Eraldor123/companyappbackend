package com.companyapp.backend.dev;

import com.companyapp.backend.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor // Tato anotace nám automaticky vytvoří konstruktor
public class AutoSeeder implements CommandLineRunner {

    private final DataSeederService dataSeederService;
    private final AvailabilityRepository availabilityRepository; // Přidán repozitář

    @Override
    public void run(String... args) throws Exception {
        log.info("🧹 Mažu staré dostupnosti, aby nevznikl konflikt s ID...");
        availabilityRepository.deleteAll(); // Přesunuto sem!

        log.info("🚀 ZAHUČELO TO DO AUTO SEEDERU: Spouštím generování brigádníků...");

        // Zde si nastavíš počet. Teď to vygeneruje 50 lidí.
        String vysledek = dataSeederService.seedArmy(0);

        log.info("✅ HOTOVO: {}", vysledek);
    }
}