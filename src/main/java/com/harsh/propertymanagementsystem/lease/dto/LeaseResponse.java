package com.harsh.propertymanagementsystem.lease.dto;

import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseResponse {

    private Long id;

    private Long propertyId;
    private String propertyName;

    private Long tenantId;
    private String tenantName;

    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;

    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;

    private Integer rentDueDay;

    private LeaseStatus status;

    private LocalDateTime createdAt;
}