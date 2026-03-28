package com.companyapp.backend.services.impl;

import com.companyapp.backend.services.dto.request.AvailabilityDTO;
import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.services.AuditLogService; // PŘIDÁNO
import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuditLogService auditLogService; // PŘIDÁNO

    // PŘIDÁNO do konstruktoru
    public AvailabilityServiceImpl(AvailabilityRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
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

        repository.deleteByUserIdAndDateRange(userId, startDate, endDate);

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

        // ZÁZNAM DO AUDITU
        // Zjistíme, jestli si dostupnost ukládá brigádník sám sobě, nebo mu ji mění manažer
        String currentUserEmail = "Neznámý";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception ignored) {}

        auditLogService.logAction(
                "UPDATE_AVAILABILITY",
                "Availability",
                userId.toString(),
                "Uložena/změněna měsíční dostupnost (" + yearMonth + ") pro uživatele s ID " + userId + ". Akci provedl: " + currentUserEmail
        );
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