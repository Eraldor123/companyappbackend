package com.companyapp.backend.services.dto.request;

import com.companyapp.backend.enums.AccessLevel;
import com.companyapp.backend.enums.ContractType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive; // PŘIDÁNO
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Jméno je povinné.")
    private String firstName;

    @NotBlank(message = "Příjmení je povinné.")
    private String lastName;

    @Email(message = "E-mail nemá správný formát.")
    @NotBlank(message = "E-mail je povinný.")
    private String email;

    private String phone;

    @NotNull(message = "Smluvní vztah musí být specifikován.")
    private ContractType contractType; // DPP, HPP, OSVC

    @NotEmpty(message = "Musí být zvolena alespoň jedna úroveň přístupu.")
    private Set<AccessLevel> accessLevels;

    // --- Specifická pole závislá na typu úvazku ---

    @Positive(message = "Hodinová mzda musí být kladné číslo.")
    private BigDecimal hourlyWage;     // Pro DPP / HPP / OSVČ (pokud je placen hodinově)

    @Positive(message = "Měsíční mzda musí být kladné číslo.")
    private BigDecimal monthlyWage;    // Pro HPP

    @Positive(message = "Velikost úvazku musí být kladné číslo.")
    private Double contractSize;       // Pro HPP (např. 0.5 pro poloviční úvazek)

    private String paymentType;        // Pro OSVČ ("HODINOVA_SAZBA" / "FIXNI_ODMENA")

    @Positive(message = "Fixní odměna musí být kladné číslo.")
    private BigDecimal fixedReward;    // Pro OSVČ (pokud je placen fixně)

    private String ico;                // Pro OSVČ

    // UI checkbox: "Zaslat heslo"
    private boolean sendPassword;

}