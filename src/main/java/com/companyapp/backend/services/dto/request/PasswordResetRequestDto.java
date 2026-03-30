package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDto {
    @Email(message = "Neplatný formát e-mailu")
    @NotBlank(message = "E-mail nesmí být prázdný")
    private String email;
}