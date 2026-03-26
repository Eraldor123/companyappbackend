package com.companyapp.backend.services.impl;

import com.companyapp.backend.entity.Shift;
import com.companyapp.backend.repository.ShiftRepository;
import com.companyapp.backend.services.ShiftService;
import com.companyapp.backend.services.dto.request.ShiftUpdateRequest;
import com.companyapp.backend.services.dto.response.ShiftDto;
import com.companyapp.backend.services.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;

    @Override
    @Transactional
    public ShiftDto updateShift(UUID id, ShiftUpdateRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Směna nebyla nalezena."));

        // Převod LocalDateTime na ZonedDateTime pomocí systémového pásma
        // .atZone(ZoneId.systemDefault()) k tomu přidá to chybějící "+01:00"
        shift.setStartTime(request.getStartTime().atZone(java.time.ZoneId.systemDefault()));
        shift.setEndTime(request.getEndTime().atZone(java.time.ZoneId.systemDefault()));

        shift.setRequiredCapacity(request.getRequiredCapacity());

        Shift savedShift = shiftRepository.save(shift);
        return mapToDto(savedShift);
    }

    private ShiftDto mapToDto(Shift shift) {
        // Předpokládám, že ShiftDto má pole typu ZonedDateTime
        return ShiftDto.builder()
                .id(shift.getId())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .requiredCapacity(shift.getRequiredCapacity())
                .stationId(shift.getStation().getId())
                .build();
    }
}