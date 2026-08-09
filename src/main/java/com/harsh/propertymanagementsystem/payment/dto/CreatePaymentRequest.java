package com.harsh.propertymanagementsystem.payment.dto;

import com.harsh.propertymanagementsystem.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotNull
    private Long leaseId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate paymentDate;

    private LocalDate dueDate;

    @NotNull
    private PaymentMethod paymentMethod;

    private String transactionReference;
}