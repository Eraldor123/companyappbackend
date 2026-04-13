package com.companyapp.backend.services.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AssignedUserDto {
    private UUID userId;
    private String name;
    @JsonProperty("isCollision")
    private boolean isCollision;
}