package com.companyapp.backend.services;

import com.companyapp.backend.services.dto.response.ShiftAssignmentDto;

import java.util.UUID;

public interface ShiftAssignmentService {
    ShiftAssignmentDto assignShift(UUID shiftId, UUID userId);
    void removeAssignment(UUID shiftAssignmentId);
}