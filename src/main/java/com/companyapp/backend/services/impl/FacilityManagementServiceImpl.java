package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.MainCategory;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.repository.MainCategoryRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.services.FacilityManagementService;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityManagementServiceImpl implements FacilityManagementService {

    private final StationRepository stationRepository;
    private final MainCategoryRepository mainCategoryRepository;

    @Override
    @Transactional
    public void deactivateStation(UUID stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno."));

        station.setActive(false);
        stationRepository.save(station);
        log.info("Stanoviště {} bylo bezpečně deaktivováno.", station.getName());
    }

    @Override
    @Transactional
    public void deactivateMainCategory(UUID categoryId) {
        MainCategory category = mainCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie nenalezena."));

        category.setActive(false);
        mainCategoryRepository.save(category);
        log.info("Kategorie {} byla deaktivována. Historická data zůstávají nedotčena.", category.getName());
    }

    // Metoda navíc pro ukázku tvrdého smazání vyžadovaného UI (např. stránka 73 z návrhu)
    @Transactional
    public void hardDeleteStation(UUID stationId, String confirmationText) {
        if (!"DELETE".equals(confirmationText)) {
            throw new IllegalArgumentException("Pro trvalé smazání musíte zadat text 'DELETE'.");
        }

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno."));

        stationRepository.delete(station);
        log.warn("Stanoviště {} bylo TRVALE smazáno vč. kaskádových vazeb!", station.getName());
    }
}