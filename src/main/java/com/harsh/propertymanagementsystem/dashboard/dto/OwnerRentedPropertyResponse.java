package com.harsh.propertymanagementsystem.dashboard.dto;

import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
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
public class OwnerRentedPropertyResponse {
    // Property details
    private Long propertyId;
    private String propertyName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private PropertyType propertyType;
    private PropertyStatus status;
    private BigDecimal propertyRentAmount;
    private BigDecimal propertySecurityDeposit;
    private Integer totalUnits;
    private LocalDateTime propertyCreatedAt;

    // Active Lease details
    private Long leaseId;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
    private BigDecimal monthlyRent;
    private BigDecimal leaseSecurityDeposit;
    private Integer rentDueDay;
    private LeaseStatus leaseStatus;

    // Tenant details
    private Long tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhoneNumber;

    // Financial & Maintenance details
    private String rentStatus;
    private BigDecimal totalRentCollected;
    private long openMaintenanceRequestsCount;
}
