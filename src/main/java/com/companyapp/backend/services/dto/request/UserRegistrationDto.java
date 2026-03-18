package com.companyapp.backend.services.dto.request;

import com.companyapp.backend.enums.AccessLevel;
import com.companyapp.backend.enums.ContractType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

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

    @NotBlank(message = "Docházkové ID je povinné.")
    private String attendanceId;

    @NotNull(message = "Smluvní vztah musí být specifikován.")
    private ContractType contractType; // DPP, HPP, OSVC

    @NotNull(message = "Úroveň přístupu musí být zvolena.")
    private AccessLevel accessLevel;

    // --- Specifická pole závislá na typu úvazku ---
    private BigDecimal hourlyWage;     // Pro DPP / HPP / OSVČ (pokud je placen hodinově)
    private BigDecimal monthlyWage;    // Pro HPP
    private Double contractSize;       // Pro HPP (např. 0.5 pro poloviční úvazek)
    private String paymentType;        // Pro OSVČ ("HODINOVA_SAZBA" / "FIXNI_ODMENA")
    private BigDecimal fixedReward;    // Pro OSVČ (pokud je placen fixně)
    private String ico;                // Pro OSVČ

    // UI checkbox: "Zaslat heslo"
    private boolean sendPassword;
}