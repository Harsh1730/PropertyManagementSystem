package com.harsh.propertymanagementsystem.payment.mapper;

import com.harsh.propertymanagementsystem.payment.dto.CreatePaymentRequest;
import com.harsh.propertymanagementsystem.payment.dto.PaymentResponse;
import com.harsh.propertymanagementsystem.payment.entity.Payment;
import com.harsh.propertymanagementsystem.payment.entity.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(CreatePaymentRequest request) {
        return Payment.builder()
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .dueDate(request.getDueDate())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(request.getTransactionReference())
                .build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getLease().getId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getCreatedAt()
        );
    }
}