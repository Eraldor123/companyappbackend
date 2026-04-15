package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.MainCategory;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.repository.MainCategoryRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.FacilityManagementService;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityManagementServiceImpl implements FacilityManagementService {

    private final StationRepository stationRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public void deactivateStation(Integer stationId) {
        // OPRAVA: Odstraněna mezera v "nenalezeno"
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno."));

        station.setActive(false);
        stationRepository.save(station);
        log.info("Stanoviště {} bylo bezpečně deaktivováno.", station.getName());

        auditLogService.logAction(
                "DEACTIVATE_STATION",
                "Station",
                stationId.toString(),
                "Stanoviště '" + station.getName() + "' (ID: " + stationId + ") bylo deaktivováno."
        );
    }

    @Override
    @Transactional
    public void deactivateMainCategory(Integer categoryId) {
        MainCategory category = mainCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie nenalezena."));

        category.setActive(false);
        mainCategoryRepository.save(category);
        log.info("Kategorie {} byla deaktivována. Historická data zůstávají nedotčena.", category.getName());

        auditLogService.logAction(
                "DEACTIVATE_CATEGORY",
                "MainCategory",
                categoryId.toString(),
                "Hlavní kategorie '" + category.getName() + "' (ID: " + categoryId + ") byla deaktivována."
        );
    }

    /**
     * OPRAVA java:S1144: Metoda je ponechána pro potřeby údržby databáze
     * nebo administrátorské zásahy.
     */
    @Override
    @Transactional
    @SuppressWarnings("unused")
    public void hardDeleteStation(Integer stationId, String confirmationText) {
        if (!"DELETE".equals(confirmationText)) {
            throw new IllegalArgumentException("Pro trvalé smazání musíte zadat text 'DELETE'.");
        }

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno."));

        String stationName = station.getName();
        stationRepository.delete(station);
        log.warn("Stanoviště {} bylo TRVALE smazáno vč. kaskádových vazeb!", stationName);

        auditLogService.logAction(
                "HARD_DELETE_STATION",
                "Station",
                stationId.toString(),
                "Stanoviště '" + stationName + "' bylo TRVALE SMAZÁNO z databáze (Hard Delete)."
        );
    }
}