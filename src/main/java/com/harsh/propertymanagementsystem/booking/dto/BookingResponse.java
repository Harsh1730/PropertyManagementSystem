package com.harsh.propertymanagementsystem.booking.dto;

import com.harsh.propertymanagementsystem.booking.entity.BookingStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;

    // Property Details
    private Long propertyId;
    private String propertyName;
    private String propertyAddress;
    private String propertyCity;
    private String propertyState;
    private PropertyType propertyType;
    private PropertyStatus propertyStatus;

    // Tenant Details
    private Long tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhoneNumber;

    // Owner Details
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhoneNumber;

    // Booking Terms
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private String message;
    private BookingStatus status;

    // Lease link if approved
    private Long leaseId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
