package com.harsh.propertymanagementsystem.dashboard.dto;

import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantLeaseDetailResponse {
    // Lease details
    private Long leaseId;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private Integer rentDueDay;
    private LeaseStatus status;
    private LocalDateTime createdAt;

    // Property details
    private Long propertyId;
    private String propertyName;
    private String propertyDescription;
    private String propertyAddress;
    private String propertyCity;
    private String propertyState;
    private String propertyCountry;
    private String propertyPostalCode;
    private PropertyType propertyType;

    // Owner / Landlord details
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhoneNumber;

    // Financial & Maintenance summary for this lease
    private String rentStatus;
    private BigDecimal totalRentPaid;
    private long openMaintenanceRequestsCount;
}
