package com.companyapp.backend.services;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShiftGenerationService {
    // Návratový typ by byl List<ShiftDto>, pro stručnost vynecháno
    List<Object> generateShiftsFromTemplate(LocalDate startDate, LocalDate endDate, UUID templateId);
    void copyWeekSchedule(LocalDate sourceWeekStart, LocalDate targetWeekStart);
}