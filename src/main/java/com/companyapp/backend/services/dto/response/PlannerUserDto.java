package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class PlannerUserDto {
    private UUID userId;
    private String name;

    // Seznam ID stanovišť, kam ho můžeme zařadit (aby to ve směnáři mohlo svítit zeleně)
    private List<Integer> qualifiedStationIds;

    // Mapa: Datum (např. "2026-03-30") -> Typ dostupnosti ("DOP", "ODP", "CELÝ DEN")
    private Map<String, String> weekAvailability;

    // Statistiky pro ty dva fialové čtverce z tvé Figmy
    private int plannedShiftsThisMonth;
    private int completedShiftsThisMonth;
}