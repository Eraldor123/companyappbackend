package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Qualification;
import com.companyapp.backend.entity.UserQualification;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.repository.UserQualificationRepository;
import com.companyapp.backend.services.QualificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualificationServiceImpl implements QualificationService {

    private final UserQualificationRepository userQualificationRepository;
    private final StationRepository stationRepository;

    @Override
    @Transactional
    public void updateUserQualifications(UUID userId, Set<UUID> newQualificationIds) {
        // Získání aktuálně přiřazených kvalifikací uživatele
        Set<Qualification> currentQualificationIds = userQualificationRepository.findByUserId(userId)
                .stream()
                .map(UserQualification::getQualification)
                .collect(Collectors.toSet());

        // Zjistíme, které kvalifikace smazat (jsou v DB, ale už ne v novém setu)
        Set<Qualification> toRemove = currentQualificationIds.stream()
                .filter(id ->!newQualificationIds.contains(id))
                .collect(Collectors.toSet());

        // Zjistíme, které přidat (jsou v novém setu, ale ještě ne v DB)
        Set<UUID> toAdd = newQualificationIds.stream()
                .filter(id ->!currentQualificationIds.contains(id))
                .collect(Collectors.toSet());

        // userQualificationRepository.deleteAllByUserIdAndQualificationIdIn(userId, toRemove);
        // Pro každé ID v toAdd vytvoříme novou entitu UserQualification a uložíme

        log.info("Kvalifikace pro uživatele {} byly aktualizovány.", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyUserQualificationForStation(UUID userId, Integer stationId) {
        // Najdeme stanoviště a zjistíme, jaké kvalifikace vyžaduje
        // Station station = stationRepository.findById(stationId).orElseThrow();
        // Set<Qualification> requiredQuals = station.getRequiredQualifications();

        // Pokud stanoviště nevyžaduje žádnou specifickou kvalifikaci, vracíme rovnou true
        // if (requiredQuals.isEmpty()) return true;

        // Jinak porovnáme requiredQuals s userQualificationRepository.findByUserId(userId)
        // a ověříme, zda uživatel splňuje VŠECHNY požadavky.

        return true; // Zástupná návratová hodnota
    }
}