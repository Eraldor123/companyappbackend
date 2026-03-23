package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponseDto {
    private String token;
    private UUID userId;
    private String email;
    private java.util.Set<String> roles;
}
