package com.harsh.propertymanagementsystem.property.mapper;

import com.harsh.propertymanagementsystem.property.dto.CreatePropertyRequest;
import com.harsh.propertymanagementsystem.property.dto.PropertyResponse;
import com.harsh.propertymanagementsystem.property.entity.Property;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {
    public Property toEntity(CreatePropertyRequest request) {
        return Property.builder()
                .propertyName(request.getPropertyName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .propertyType(request.getPropertyType())
                .rentAmount(request.getRentAmount())
                .securityDeposit(request.getSecurityDeposit())
                .totalUnits(request.getTotalUnits())
                .build();
    }

    public PropertyResponse toResponse(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .propertyName(property.getPropertyName())
                .description(property.getDescription())
                .address(property.getAddress())
                .city(property.getCity())
                .state(property.getState())
                .country(property.getCountry())
                .postalCode(property.getPostalCode())
                .propertyType(property.getPropertyType())
                .status(property.getStatus())
                .rentAmount(property.getRentAmount())
                .securityDeposit(property.getSecurityDeposit())
                .totalUnits(property.getTotalUnits())
                .createdAt(property.getCreatedAt())
                .build();
    }
}