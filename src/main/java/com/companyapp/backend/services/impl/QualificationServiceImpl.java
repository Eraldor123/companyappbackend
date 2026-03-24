package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Station;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.repository.UserQualificationRepository;
import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualificationServiceImpl implements QualificationService {

    private final UserQualificationRepository userQualificationRepository;
    private final StationRepository stationRepository;

    @Override
    @Transactional
    public void updateUserQualifications(UUID userId, Set<UUID> newQualificationIds) {
        // Tato metoda zatím zůstává pro budoucí správu konkrétních certifikátů uživatele,
        // ale pro základní funkčnost přepínače na stanovišti není momentálně kritická.
        log.info("Požadavek na aktualizaci kvalifikací pro uživatele {}.", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyUserQualificationForStation(UUID userId, Integer stationId) {
        // 1. Najdeme stanoviště
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno."));

        // 2. Pokud stanoviště nevyžaduje kvalifikaci (needsQualification == false),
        // může tam pracovat kdokoliv.
        if (Boolean.FALSE.equals(station.getNeedsQualification())) {
            return true;
        }

        // 3. Pokud stanoviště kvalifikaci vyžaduje, zkontrolujeme, zda má uživatel
        // alespoň jeden záznam v tabulce user_qualifications.
        // (Zjednodušená logika: máš-li jakoukoliv kvalifikaci, můžeš na stanoviště s příznakem).
        return !userQualificationRepository.findByUserId(userId).isEmpty();
    }
}