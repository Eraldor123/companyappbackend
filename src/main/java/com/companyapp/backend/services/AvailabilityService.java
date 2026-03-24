package com.companyapp.backend.services;

import com.companyapp.backend.dto.AvailabilityDTO;
import com.companyapp.backend.services.dto.request.MonthlyAvailabilityRequestDto;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {

    // Načtení dat pomocí UUID a YearMonth
    List<AvailabilityDTO> getMonthlyAvailability(UUID userId, YearMonth yearMonth);

    // Uložení dat rovnou z tvého DTO
    void saveMonthlyAvailability(MonthlyAvailabilityRequestDto request);
}