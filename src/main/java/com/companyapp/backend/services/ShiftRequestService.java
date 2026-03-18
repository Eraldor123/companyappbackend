package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.request.ShiftCancellationRequestDto;

public interface ShiftRequestService {
    void submitCancellationRequest(ShiftCancellationRequestDto request);
}