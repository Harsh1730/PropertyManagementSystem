package com.harsh.propertymanagementsystem.lease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaseRequest {

    @NotNull
    private Long propertyId;

    @NotNull
    private Long tenantId;

    @NotNull
    private LocalDate leaseStartDate;

    @NotNull
    private LocalDate leaseEndDate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal monthlyRent;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal securityDeposit;

    @NotNull
    @Min(1)
    @Max(31)
    private Integer rentDueDay;
}