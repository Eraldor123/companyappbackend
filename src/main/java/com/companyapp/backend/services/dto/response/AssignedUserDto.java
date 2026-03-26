package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AssignedUserDto {
    private UUID userId;
    private String name;
}