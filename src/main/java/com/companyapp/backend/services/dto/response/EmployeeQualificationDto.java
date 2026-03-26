package com.companyapp.backend.services.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EmployeeQualificationDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String contractType;
    private String photoUrl;
    private List<Integer> qualifiedStationIds;
}
