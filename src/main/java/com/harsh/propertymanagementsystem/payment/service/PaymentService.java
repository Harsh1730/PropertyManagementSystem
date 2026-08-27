package com.harsh.propertymanagementsystem.payment.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.lease.entity.Lease;
import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import com.harsh.propertymanagementsystem.lease.repository.LeaseRepository;
import com.harsh.propertymanagementsystem.payment.dto.CreatePaymentRequest;
import com.harsh.propertymanagementsystem.payment.dto.PaymentResponse;
import com.harsh.propertymanagementsystem.payment.dto.RentStatusResponse;
import com.harsh.propertymanagementsystem.payment.entity.Payment;
import com.harsh.propertymanagementsystem.payment.entity.PaymentStatus;
import com.harsh.propertymanagementsystem.payment.mapper.PaymentMapper;
import com.harsh.propertymanagementsystem.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        User user = getCurrentUser();

        Lease lease = leaseRepository.findById(request.getLeaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + request.getLeaseId()));

        if (!lease.getTenant().getId().equals(user.getId())) {
            log.warn("User {} is not the tenant of lease {}", user.getEmail(), lease.getId());
            throw new AccessDeniedException("You are not the tenant of this lease");
        }

        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new IllegalStateException("Payment cannot be made for an inactive lease (status: " + lease.getStatus() + ")");
        }

        Payment payment = paymentMapper.toEntity(request);
        payment.setLease(lease);
        payment.setStatus(PaymentStatus.PAID);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Processed payment {} of amount {} for lease {}", savedPayment.getId(), savedPayment.getAmount(), lease.getId());
        return paymentMapper.toResponse(savedPayment);
    }

    public List<PaymentResponse> getMyPayments() {
        User user = getCurrentUser();
        return paymentRepository.findByLeaseTenantId(user.getId())
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public List<PaymentResponse> getOwnerPayments() {
        User owner = getCurrentUser();
        return paymentRepository.findByLeasePropertyOwnerId(owner.getId())
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public List<PaymentResponse> getLeasePayments(Long leaseId) {
        User user = getCurrentUser();

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + leaseId));

        boolean isTenant = lease.getTenant().getId().equals(user.getId());
        boolean isOwner = lease.getProperty().getOwner().getId().equals(user.getId());

        if (!isTenant && !isOwner) {
            log.warn("Access denied: User {} cannot view payments for lease {}", user.getEmail(), leaseId);
            throw new AccessDeniedException("You do not have access to this lease");
        }

        return paymentRepository.findByLeaseId(leaseId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public RentStatusResponse getRentStatus(Long leaseId) {
        User user = getCurrentUser();

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + leaseId));

        boolean isTenant = lease.getTenant().getId().equals(user.getId());
        boolean isOwner = lease.getProperty().getOwner().getId().equals(user.getId());

        if (!isTenant && !isOwner) {
            log.warn("Access denied: User {} cannot view rent status for lease {}", user.getEmail(), leaseId);
            throw new AccessDeniedException("You do not have access to this lease");
        }

        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        Optional<Payment> payment = paymentRepository
                .findFirstByLeaseIdAndPaymentDateBetweenOrderByPaymentDateDesc(
                        leaseId,
                        startOfMonth,
                        endOfMonth
                );

        String status;
        if (payment.isPresent()
                && payment.get().getStatus() == PaymentStatus.PAID
                && payment.get().getAmount().compareTo(lease.getMonthlyRent()) >= 0) {
            status = "PAID";
        } else if (today.getDayOfMonth() <= lease.getRentDueDay()) {
            status = "DUE";
        } else {
            status = "OVERDUE";
        }

        return new RentStatusResponse(
                lease.getId(),
                lease.getMonthlyRent(),
                lease.getRentDueDay(),
                status
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No authenticated user found");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}