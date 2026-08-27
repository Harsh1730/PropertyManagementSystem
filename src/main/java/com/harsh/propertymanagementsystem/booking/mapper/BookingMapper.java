package com.harsh.propertymanagementsystem.booking.mapper;

import com.harsh.propertymanagementsystem.booking.dto.BookingResponse;
import com.harsh.propertymanagementsystem.booking.entity.BookingRequest;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(BookingRequest entity) {
        if (entity == null) {
            return null;
        }

        return BookingResponse.builder()
                .id(entity.getId())
                // Property details
                .propertyId(entity.getProperty() != null ? entity.getProperty().getId() : null)
                .propertyName(entity.getProperty() != null ? entity.getProperty().getPropertyName() : null)
                .propertyAddress(entity.getProperty() != null ? entity.getProperty().getAddress() : null)
                .propertyCity(entity.getProperty() != null ? entity.getProperty().getCity() : null)
                .propertyState(entity.getProperty() != null ? entity.getProperty().getState() : null)
                .propertyType(entity.getProperty() != null ? entity.getProperty().getPropertyType() : null)
                .propertyStatus(entity.getProperty() != null ? entity.getProperty().getStatus() : null)
                // Tenant details
                .tenantId(entity.getTenant() != null ? entity.getTenant().getId() : null)
                .tenantName(entity.getTenant() != null ? entity.getTenant().getName() : null)
                .tenantEmail(entity.getTenant() != null ? entity.getTenant().getEmail() : null)
                .tenantPhoneNumber(entity.getTenant() != null ? entity.getTenant().getPhoneNumber() : null)
                // Owner details
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .ownerName(entity.getOwner() != null ? entity.getOwner().getName() : null)
                .ownerEmail(entity.getOwner() != null ? entity.getOwner().getEmail() : null)
                .ownerPhoneNumber(entity.getOwner() != null ? entity.getOwner().getPhoneNumber() : null)
                // Booking terms
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .monthlyRent(entity.getMonthlyRent())
                .securityDeposit(entity.getSecurityDeposit())
                .message(entity.getMessage())
                .status(entity.getStatus())
                // Lease link
                .leaseId(entity.getLease() != null ? entity.getLease().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
