package com.companyapp.backend.services.impl;

import com.companyapp.backend.dto.AvailabilityDTO;
import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository repository;

    public AvailabilityServiceImpl(AvailabilityRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityDTO> getMonthlyAvailability(UUID userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Availability> entityList = repository.findByUserIdAndDateRange(userId, startDate, endDate);

        return entityList.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveMonthlyAvailability(MonthlyAvailabilityRequestDto request) {
        UUID userId = request.getUserId();
        YearMonth yearMonth = request.getMonth();

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Smažeme stará data
        repository.deleteByUserIdAndDateRange(userId, startDate, endDate);

        // Uložíme nová data z Request DTO
        List<Availability> newEntities = request.getAvailableDays().stream().map(dto -> {
            Availability entity = new Availability();
            entity.setUserId(userId);
            entity.setAvailableDate(dto.getDate());
            entity.setMorning(dto.isMorning());
            entity.setAfternoon(dto.isAfternoon());
            entity.setConfirmed(false);
            return entity;
        }).collect(Collectors.toList());

        repository.saveAll(newEntities);
    }

    private AvailabilityDTO mapToDTO(Availability entity) {
        return new AvailabilityDTO(
                entity.getId(),
                entity.getAvailableDate(),
                entity.isMorning(),
                entity.isAfternoon(),
                entity.isConfirmed()
        );
    }
}