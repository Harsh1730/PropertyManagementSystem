package com.harsh.propertymanagementsystem.payment.controller;

import com.harsh.propertymanagementsystem.common.exception.GlobalExceptionHandler;
import com.harsh.propertymanagementsystem.payment.dto.CreatePaymentRequest;
import com.harsh.propertymanagementsystem.payment.dto.PaymentResponse;
import com.harsh.propertymanagementsystem.payment.dto.RentStatusResponse;
import com.harsh.propertymanagementsystem.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentResponse createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @GetMapping("/my")
    public List<PaymentResponse> getMyPayments() {
        return paymentService.getMyPayments();
    }

    @GetMapping("/owner")
    public List<PaymentResponse> getOwnerPayments() {
        return paymentService.getOwnerPayments();
    }

    @GetMapping("/lease/{leaseId}")
    public List<PaymentResponse> getLeasePayments(
            @PathVariable Long leaseId) {
        return paymentService.getLeasePayments(leaseId);
    }

    @GetMapping("/lease/{leaseId}/status")
    public RentStatusResponse getRentStatus(
            @PathVariable Long leaseId)
            throws GlobalExceptionHandler.ResourceNotFoundException {
        return paymentService.getRentStatus(leaseId);
    }
} 