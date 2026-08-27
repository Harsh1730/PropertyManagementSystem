package com.harsh.propertymanagementsystem.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TenantDashboardResponse(
        Long activeLeaseId,
        Long propertyId,
        String propertyName,
        String propertyAddress,
        BigDecimal monthlyRent,
        String rentStatus,
        Integer rentDueDay,
        LocalDate leaseStartDate,
        LocalDate leaseEndDate,
        BigDecimal totalPayments,
        long openMaintenanceRequests
) {
}