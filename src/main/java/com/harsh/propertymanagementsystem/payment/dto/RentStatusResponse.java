package com.harsh.propertymanagementsystem.payment.dto;

import java.math.BigDecimal;

public record RentStatusResponse(
        Long leaseId,
        BigDecimal monthlyRent,
        Integer rentDueDay,
        String status
) {
}