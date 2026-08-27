package com.harsh.propertymanagementsystem.dashboard.dto;

import java.math.BigDecimal;

public record OwnerDashboardResponse(
        long totalProperties,
        long occupiedProperties,
        long availableProperties,
        long activeLeases,
        BigDecimal totalRentCollected,
        long pendingMaintenanceRequests
) {
}