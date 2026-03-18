package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDto {
    @NotBlank(message = "E-mail je povinný")
    private String email;

    @NotBlank(message = "Heslo/PIN je povinné")
    private String password;
}
