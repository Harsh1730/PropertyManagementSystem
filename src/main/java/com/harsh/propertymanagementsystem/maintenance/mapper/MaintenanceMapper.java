package com.harsh.propertymanagementsystem.maintenance.mapper;

import com.harsh.propertymanagementsystem.maintenance.dto.CreateMaintenanceRequest;
import com.harsh.propertymanagementsystem.maintenance.dto.MaintenanceResponse;
import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceRequest;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceMapper {

    public MaintenanceRequest toEntity(CreateMaintenanceRequest request) {
        return MaintenanceRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();
    }

    public MaintenanceResponse toResponse(MaintenanceRequest request) {
        return new MaintenanceResponse(
                request.getId(),
                request.getProperty().getId(),
                request.getTenant().getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}