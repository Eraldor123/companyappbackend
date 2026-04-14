// src/main/java/com/companyapp/backend/services/ShiftGenerationService.java
package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.ShiftDto; // PŘIDÁNO
import java.time.LocalDate;
import java.util.List;

public interface ShiftGenerationService {
    /**
     * FÁZE 3: Oprava návratového typu na ShiftDto.
     * Tímto krokem definujeme pevnou strukturu odpovědi a předcházíme úniku entit.
     */
    List<ShiftDto> generateShiftsFromTemplate(LocalDate startDate, LocalDate endDate, Integer templateId);

    void copyWeekSchedule(LocalDate sourceWeekStart, LocalDate targetWeekStart);
    void clearWeekSchedule(LocalDate startDate, LocalDate endDate);
    void generateCustomShifts(com.companyapp.backend.services.dto.request.CreateCustomShiftRequestDto request);
}