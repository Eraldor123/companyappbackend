package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Contract;
import com.companyapp.backend.entity.Station;
import com.companyapp.backend.entity.User;
import com.companyapp.backend.entity.UserProfile;
import com.companyapp.backend.repository.ContractRepository;
import com.companyapp.backend.repository.StationRepository;
import com.companyapp.backend.repository.UserRepository;
import com.companyapp.backend.services.QualificationService;
import com.companyapp.backend.services.dto.response.EmployeeQualificationDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QualificationServiceImpl implements QualificationService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeQualificationDto> getAllEmployeesWithStations() {
        List<User> users = userRepository.findAllActiveUsersWithDetails();

        return users.stream().map(user -> {
            UserProfile profile = user.getUserProfile();
            Contract contract = contractRepository.findLatestContractByUserId(user.getId()).orElse(null);

            List<Integer> stationIds = user.getQualifiedStations().stream()
                    .map(Station::getId)
                    .collect(Collectors.toList());

            return EmployeeQualificationDto.builder()
                    .id(user.getId())
                    .firstName(profile != null ? profile.getFirstName() : "Neznámé")
                    .lastName(profile != null ? profile.getLastName() : "Neznámé")
                    .contractType(contract != null ? contract.getType().name() : "N/A")
                    .photoUrl(profile != null ? profile.getProfilePictureUrl() : "")
                    .qualifiedStationIds(stationIds)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateUserStations(UUID userId, Set<Integer> stationIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nenalezen."));

        List<Station> stations = stationRepository.findAllById(stationIds);

        // Hibernate se postará o vyčištění tabulky user_qualified_stations a vložení nových ID
        user.setQualifiedStations(new HashSet<>(stations));
        userRepository.save(user);
        log.info("Přiřazená stanoviště byla aktualizována pro uživatele {}.", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserQualifiedForStation(UUID userId, Integer stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Stanoviště nenalezeno."));

        // Pokud stanoviště nevyžaduje kvalifikaci, může tam kdokoliv
        if (Boolean.FALSE.equals(station.getNeedsQualification())) {
            return true;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Uživatel nenalezen."));

        return user.getQualifiedStations().contains(station);
    }
}