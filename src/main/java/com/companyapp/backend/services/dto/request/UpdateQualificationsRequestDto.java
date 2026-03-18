package com.companyapp.backend.services.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateQualificationsRequestDto {
    @NotNull(message = "Seznam ID kvalifikací nesmí být null.")
    private Set<Integer> qualificationIds;
}
