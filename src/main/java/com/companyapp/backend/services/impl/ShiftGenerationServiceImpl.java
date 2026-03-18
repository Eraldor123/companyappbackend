package com.companyapp.backend.services.impl;

import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.repository.ShiftTemplateRepository;
import com.companyapp.backend.services.ShiftGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftGenerationServiceImpl implements ShiftGenerationService {

    private final ShiftRepository shiftRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;

    @Override
    @Transactional
    public List<Object> generateShiftsFromTemplate(LocalDate startDate, LocalDate endDate, UUID templateId) {
        // Vygenerování "prázdných židlí" (směn) na základě šablony v cyklu od startDate do endDate
        return null;
    }

    @Override
    @Transactional
    public void copyWeekSchedule(LocalDate sourceWeekStart, LocalDate targetWeekStart) {}
}