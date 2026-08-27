package com.harsh.propertymanagementsystem.maintenance.dto;

import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceStatus;

import java.time.LocalDateTime;

public record MaintenanceResponse(
        Long id,
        Long propertyId,
        Long tenantId,
        String title,
        String description,
        MaintenanceStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}