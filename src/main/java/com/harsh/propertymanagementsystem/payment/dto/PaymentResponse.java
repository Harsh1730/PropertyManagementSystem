package com.harsh.propertymanagementsystem.payment.dto;

import com.harsh.propertymanagementsystem.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long leaseId,
        BigDecimal amount,
        LocalDate paymentDate,
        PaymentStatus status,
        com.harsh.propertymanagementsystem.payment.entity.PaymentMethod paymentMethod,
        String transactionReference,
        LocalDateTime createdAt
) {
}