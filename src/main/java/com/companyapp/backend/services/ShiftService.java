package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.ShiftUpdateRequest;
import com.companyapp.backend.services.dto.response.ShiftDto;
import java.util.UUID;

public interface ShiftService {
    ShiftDto updateShift(UUID id, ShiftUpdateRequest request);
}