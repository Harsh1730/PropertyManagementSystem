package com.harsh.propertymanagementsystem.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMaintenanceRequest {

    @NotNull
    private Long propertyId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;
}