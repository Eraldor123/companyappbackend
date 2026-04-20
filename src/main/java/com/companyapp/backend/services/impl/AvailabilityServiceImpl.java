package com.companyapp.backend.services.impl;

import com.companyapp.backend.config.CheckOwnership;
import com.companyapp.backend.entity.Availability;
import com.companyapp.backend.entity.ShiftAssignment;
import com.companyapp.backend.repository.AvailabilityRepository;
import com.companyapp.backend.repository.ShiftAssignmentRepository;
import com.companyapp.backend.services.AuditLogService;
import com.companyapp.backend.services.AvailabilityService;
import com.companyapp.backend.services.dto.request.AvailabilityDayDto;
import com.companyapp.backend.services.dto.request.AvailabilityDTO;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository repository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AuditLogService auditLogService;

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityDTO> getMonthlyAvailability(@CheckOwnership UUID userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Availability> entityList = repository.findByUserIdAndDateRange(userId, startDate, endDate);
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(List.of(userId), startDate, endDate);

        Map<LocalDate, AvailabilityDTO> dtoMap = new HashMap<>();

        // 1. Zpracujeme dny fyzicky uložené v tabulce Availabilities
        for (Availability entity : entityList) {
            dtoMap.put(entity.getAvailableDate(), mapToAvailabilityDTO(entity, assignments));
        }

        // 2. Doplníme "virtuální" dostupnost pro dny se směnou, kterou uživatel v kalendáři nepotvrdil
        for (ShiftAssignment sa : assignments) {
            LocalDate shiftDate = sa.getShift().getShiftDate();
            if (!dtoMap.containsKey(shiftDate)) {
                Availability dummyEntity = new Availability();
                dummyEntity.setUserId(userId);
                dummyEntity.setAvailableDate(shiftDate);
                dummyEntity.setMorning(isMorningShift(sa));
                dummyEntity.setAfternoon(isAfternoonShift(sa));
                dummyEntity.setConfirmed(true);

                dtoMap.put(shiftDate, mapToAvailabilityDTO(dummyEntity, assignments));
            }
        }

        return new ArrayList<>(dtoMap.values());
    }

    @Override
    @Transactional
    public void saveMonthlyAvailability(@CheckOwnership MonthlyAvailabilityRequestDto request) {
        UUID userId = request.getUserId();
        YearMonth yearMonth = request.getMonth();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Availability> existingAvailabilities = repository.findByUserIdAndDateRange(userId, startDate, endDate);
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAssignmentsForUsersInDateRange(List.of(userId), startDate, endDate);

        repository.deleteAll(existingAvailabilities);

        List<Availability> newEntities = new ArrayList<>(request.getAvailableDays().stream()
                .map(dto -> createAvailabilityEntity(userId, dto, assignments))
                .filter(Objects::nonNull)
                .toList());

        ensureAvailabilityForExistingShifts(userId, newEntities, assignments);

        repository.saveAll(newEntities);
        logAuditAction(userId, yearMonth);
    }

    // =========================================================================
    // DYNAMICKÁ LOGIKA PODLE NASTAVENÍ AREÁLU (MODÁLU)
    // =========================================================================

    private boolean isMorningShift(ShiftAssignment sa) {
        LocalTime shiftStart = sa.getStartTime().atZone(UTC_ZONE).toLocalTime();
        LocalTime changeTime = getSplitTime(sa);

        // Ranní je ta, co začíná PŘED zlomovým časem (např. před 14:00)
        return shiftStart.isBefore(changeTime);
    }

    private boolean isAfternoonShift(ShiftAssignment sa) {
        LocalTime shiftStart = sa.getStartTime().atZone(UTC_ZONE).toLocalTime();
        LocalTime shiftEnd = sa.getEndTime().atZone(UTC_ZONE).toLocalTime();
        LocalTime changeTime = getSplitTime(sa);

        // Odpolední je ta, co končí PO zlomovém čase, NEBO přesně v tento čas začíná
        return shiftEnd.isAfter(changeTime) || shiftStart.equals(changeTime) || shiftStart.isAfter(changeTime);
    }

    // Bezpečné získání "Zlomu směn" z databáze
    private LocalTime getSplitTime(ShiftAssignment sa) {
        if (sa.getShift() != null &&
                sa.getShift().getStation() != null &&
                sa.getShift().getStation().getAfternoonStartTime() != null) {

            return sa.getShift().getStation().getAfternoonStartTime();
        }
        // Fallback pro staré směny/stanice, které tento údaj ještě nemají
        return LocalTime.of(14, 0);
    }

    // =========================================================================
    // POMOCNÉ MAPOVACÍ METODY
    // =========================================================================

    private AvailabilityDTO mapToAvailabilityDTO(Availability entity, List<ShiftAssignment> allAssignments) {
        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setId(entity.getId());
        dto.setDate(entity.getAvailableDate());
        dto.setMorning(entity.isMorning());
        dto.setAfternoon(entity.isAfternoon());

        enrichWithShiftData(dto, allAssignments);
        return dto;
    }

    private void enrichWithShiftData(AvailabilityDTO dto, List<ShiftAssignment> assignments) {
        StringBuilder details = new StringBuilder();
        boolean mShift = false;
        boolean aShift = false;

        for (ShiftAssignment sa : assignments) {
            if (sa.getShift().getShiftDate().equals(dto.getDate())) {
                if (isMorningShift(sa)) mShift = true;
                if (isAfternoonShift(sa)) aShift = true;

                if (!details.isEmpty()) details.append(" | ");
                details.append(sa.getShift().getStation().getName())
                        .append(" (").append(sa.getStartTime().atZone(UTC_ZONE).toLocalTime()).append("-")
                        .append(sa.getEndTime().atZone(UTC_ZONE).toLocalTime()).append(")");
            }
        }

        dto.setHasMorningShift(mShift);
        dto.setHasAfternoonShift(aShift);
        dto.setShiftDetails(details.toString());
        dto.setConfirmed(mShift || aShift);
    }

    private Availability createAvailabilityEntity(UUID userId, AvailabilityDayDto dto, List<ShiftAssignment> assignments) {
        boolean mShift = false;
        boolean aShift = false;

        for (ShiftAssignment sa : assignments) {
            if (sa.getShift().getShiftDate().equals(dto.getDate())) {
                if (isMorningShift(sa)) mShift = true;
                if (isAfternoonShift(sa)) aShift = true;
            }
        }

        Availability entity = new Availability();
        entity.setUserId(userId);
        entity.setAvailableDate(dto.getDate());
        entity.setMorning(mShift || dto.isMorning());
        entity.setAfternoon(aShift || dto.isAfternoon());
        entity.setConfirmed(mShift || aShift);

        return (entity.isMorning() || entity.isAfternoon()) ? entity : null;
    }

    private void ensureAvailabilityForExistingShifts(UUID userId, List<Availability> entities, List<ShiftAssignment> assignments) {
        for (ShiftAssignment sa : assignments) {
            LocalDate shiftDate = sa.getShift().getShiftDate();
            boolean isDaySaved = entities.stream().anyMatch(e -> e.getAvailableDate().equals(shiftDate));

            if (!isDaySaved) {
                Availability missingDay = new Availability();
                missingDay.setUserId(userId);
                missingDay.setAvailableDate(shiftDate);
                missingDay.setMorning(isMorningShift(sa));
                missingDay.setAfternoon(isAfternoonShift(sa));
                missingDay.setConfirmed(true);
                entities.add(missingDay);
            }
        }
    }

    private void logAuditAction(UUID userId, YearMonth yearMonth) {
        String currentUserEmail = "Neznámý";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception e) {
            log.debug("Audit: Nepodařilo se získat uživatele: {}", e.getMessage());
        }

        auditLogService.logAction(
                "UPDATE_AVAILABILITY", "Availability", userId.toString(),
                "Aktualizace dostupnosti (" + yearMonth + "). Provedl: " + currentUserEmail
        );
    }
}