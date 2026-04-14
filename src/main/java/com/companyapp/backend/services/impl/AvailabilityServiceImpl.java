package com.companyapp.backend.services.impl;

import com.companyapp.backend.config.CheckOwnership;
import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.AvailabilityDTO;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository repository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AuditLogService auditLogService;

    public AvailabilityServiceImpl(AvailabilityRepository repository,
                                   ShiftAssignmentRepository shiftAssignmentRepository,
                                   AuditLogService auditLogService) {
        this.repository = repository;
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    // PŘIDÁNA ANOTACE @CheckOwnership PRO KONTROLU IDOR
    public List<AvailabilityDTO> getMonthlyAvailability(@CheckOwnership UUID userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Availability> entityList = repository.findByUserIdAndDateRange(userId, startDate, endDate);
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(List.of(userId), startDate, endDate);

        return entityList.stream().map(entity -> {
            AvailabilityDTO dto = new AvailabilityDTO();
            dto.setId(entity.getId());
            dto.setDate(entity.getAvailableDate());
            dto.setMorning(entity.isMorning());
            dto.setAfternoon(entity.isAfternoon());

            // Zjištění reálných směn v tento den
            List<ShiftAssignment> dayAssignments = assignments.stream()
                    .filter(sa -> sa.getShift().getShiftDate().equals(entity.getAvailableDate()))
                    .collect(Collectors.toList());

            boolean mShift = false;
            boolean aShift = false;
            StringBuilder details = new StringBuilder();

            for (ShiftAssignment sa : dayAssignments) {
                int startHour = sa.getStartTime().atZone(ZoneId.of("UTC")).getHour();
                int endHour = sa.getEndTime().atZone(ZoneId.of("UTC")).getHour();

                if (startHour < 12) mShift = true;
                if (endHour >= 14 || startHour >= 12) aShift = true;

                if (details.length() > 0) details.append(" | ");
                details.append(sa.getShift().getStation().getName())
                        .append(" (").append(sa.getStartTime().atZone(ZoneId.of("UTC")).toLocalTime().toString())
                        .append("-").append(sa.getEndTime().atZone(ZoneId.of("UTC")).toLocalTime().toString()).append(")");
            }

            dto.setHasMorningShift(mShift);
            dto.setHasAfternoonShift(aShift);
            dto.setShiftDetails(details.toString());
            dto.setConfirmed(mShift || aShift);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    // PŘIDÁNA ANOTACE @CheckOwnership PRO KONTROLU IDOR (Díky rozhraní Ownable to umíme zkontrolovat)
    public void saveMonthlyAvailability(@CheckOwnership MonthlyAvailabilityRequestDto request) {
        UUID userId = request.getUserId();
        YearMonth yearMonth = request.getMonth();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Availability> existingAvailabilities = repository.findByUserIdAndDateRange(userId, startDate, endDate);
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(List.of(userId), startDate, endDate);

        repository.deleteAll(existingAvailabilities);

        List<Availability> newEntities = request.getAvailableDays().stream()
                .map(dto -> {
                    Availability entity = new Availability();
                    entity.setUserId(userId);
                    entity.setAvailableDate(dto.getDate());

                    // DYNAMICKÝ ŠTÍT: Kontrola existujících směn
                    boolean mShift = false;
                    boolean aShift = false;
                    for (ShiftAssignment sa : assignments) {
                        if (sa.getShift().getShiftDate().equals(dto.getDate())) {
                            int startHour = sa.getStartTime().atZone(ZoneId.of("UTC")).getHour();
                            int endHour = sa.getEndTime().atZone(ZoneId.of("UTC")).getHour();
                            if (startHour < 12) mShift = true;
                            if (endHour >= 14 || startHour >= 12) aShift = true;
                        }
                    }

                    // Backend natvrdo přepíše požadavek frontendu, pokud na danou půlku dne existuje směna!
                    entity.setMorning(mShift ? true : dto.isMorning());
                    entity.setAfternoon(aShift ? true : dto.isAfternoon());
                    entity.setConfirmed(mShift || aShift);

                    if (!entity.isMorning() && !entity.isAfternoon()) return null;
                    return entity;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Záchrana: Pokud frontend omylem den neposlal, ale je tam směna, vrátíme ho tam!
        for (ShiftAssignment sa : assignments) {
            LocalDate shiftDate = sa.getShift().getShiftDate();
            boolean isDaySaved = newEntities.stream().anyMatch(e -> e.getAvailableDate().equals(shiftDate));

            if (!isDaySaved) {
                int startHour = sa.getStartTime().atZone(ZoneId.of("UTC")).getHour();
                int endHour = sa.getEndTime().atZone(ZoneId.of("UTC")).getHour();

                Availability missingDay = new Availability();
                missingDay.setUserId(userId);
                missingDay.setAvailableDate(shiftDate);
                missingDay.setMorning(startHour < 12);
                missingDay.setAfternoon(endHour >= 14 || startHour >= 12);
                missingDay.setConfirmed(true);
                newEntities.add(missingDay);
            }
        }

        repository.saveAll(newEntities);

        String currentUserEmail = "Neznámý";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception ignored) {}

        auditLogService.logAction(
                "UPDATE_AVAILABILITY", "Availability", userId.toString(),
                "Polo-inteligentní aktualizace dostupnosti (" + yearMonth + "). Akci provedl: " + currentUserEmail
        );
    }
}