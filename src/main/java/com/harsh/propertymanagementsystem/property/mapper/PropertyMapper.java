package com.harsh.propertymanagementsystem.property.mapper;

import com.harsh.propertymanagementsystem.property.dto.CreatePropertyRequest;
import com.harsh.propertymanagementsystem.property.dto.PropertyResponse;
import com.harsh.propertymanagementsystem.property.entity.Property;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        return toResponse(property, null, 0, null);
    }

    public PropertyResponse toResponse(Property property, Double averageRating, Integer totalReviews, Double ownerRating) {
        if (property == null) {
            return null;
        }

        List<String> imageUrls = new ArrayList<>();
        if (property.getImages() != null) {
            imageUrls = property.getImages().stream()
                    .map(img -> "/properties/images/" + img.getId())
                    .toList();
        }

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
                .ownerId(property.getOwner() != null ? property.getOwner().getId() : null)
                .ownerName(property.getOwner() != null ? property.getOwner().getName() : null)
                .ownerRating(ownerRating)
                .averageRating(averageRating)
                .totalReviews(totalReviews != null ? totalReviews : 0)
                .imageUrls(imageUrls)
                .createdAt(property.getCreatedAt())
                .build();
    }
}