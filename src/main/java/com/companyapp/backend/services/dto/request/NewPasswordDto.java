package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewPasswordDto {

    @NotBlank(message = "Token nesmí být prázdný")
    private String token;

    @NotBlank(message = "Heslo nesmí být prázdné")
    @Size(min = 8, message = "Heslo musí mít alespoň 8 znaků")
    /*
     * OPRAVA java:S5852 & java:S5843:
     * Nahrazeno [0-9] za modernější a stručnější zkratku \\d.
     * Logika zůstává stejná: kontroluje přítomnost číslice, malého a velkého písmene.
     */
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "Heslo musí obsahovat číslici, malé a velké písmeno")
    private String newPassword;
}