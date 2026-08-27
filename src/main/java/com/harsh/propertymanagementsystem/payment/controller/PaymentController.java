package com.harsh.propertymanagementsystem.payment.controller;

import com.harsh.propertymanagementsystem.payment.dto.CreatePaymentRequest;
import com.harsh.propertymanagementsystem.payment.dto.PaymentResponse;
import com.harsh.propertymanagementsystem.payment.dto.RentStatusResponse;
import com.harsh.propertymanagementsystem.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        log.info("Received request to process payment for lease {}", request.getLeaseId());
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        log.info("Received request to get my payments");
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/owner")
    public ResponseEntity<List<PaymentResponse>> getOwnerPayments() {
        log.info("Received request to get owner payments");
        return ResponseEntity.ok(paymentService.getOwnerPayments());
    }

    @GetMapping("/lease/{leaseId}")
    public ResponseEntity<List<PaymentResponse>> getLeasePayments(
            @PathVariable Long leaseId) {
        log.info("Received request to get payments for lease {}", leaseId);
        return ResponseEntity.ok(paymentService.getLeasePayments(leaseId));
    }

    @GetMapping("/lease/{leaseId}/status")
    public ResponseEntity<RentStatusResponse> getRentStatus(
            @PathVariable Long leaseId) {
        log.info("Received request to get rent status for lease {}", leaseId);
        return ResponseEntity.ok(paymentService.getRentStatus(leaseId));
    }
}