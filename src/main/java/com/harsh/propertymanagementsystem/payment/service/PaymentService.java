package com.harsh.propertymanagementsystem.payment.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.GlobalExceptionHandler;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LeaseRepository leaseRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;

    public PaymentResponse createPayment(CreatePaymentRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Lease lease = leaseRepository.findById(request.getLeaseId())
                .orElseThrow(() -> new RuntimeException("Lease not found"));

        if (!lease.getTenant().getId().equals(user.getId())) {
            throw new AccessDeniedException(
                    "You are not the tenant of this lease"
            );
        }

        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Payment cannot be made for an inactive lease"
            );
        }

        Payment payment = paymentMapper.toEntity(request);

        payment.setLease(lease);
        payment.setStatus(PaymentStatus.PAID);

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    public List<PaymentResponse> getMyPayments() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return paymentRepository.findByLeaseTenantId(user.getId())
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public List<PaymentResponse> getOwnerPayments() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return paymentRepository.findByLeasePropertyOwnerId(owner.getId())
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public List<PaymentResponse> getLeasePayments(Long leaseId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new RuntimeException("Lease not found"));

        boolean isTenant = lease.getTenant().getId().equals(user.getId());

        boolean isOwner = lease.getProperty().getOwner().getId().equals(user.getId());

        if (!isTenant && !isOwner) {
            throw new AccessDeniedException(
                    "You do not have access to this lease"
            );
        }

        return paymentRepository.findByLeaseId(leaseId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
    public RentStatusResponse getRentStatus(Long leaseId) throws GlobalExceptionHandler.ResourceNotFoundException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Lease not found"));

        boolean isTenant = lease.getTenant().getId().equals(user.getId());
        boolean isOwner = lease.getProperty().getOwner().getId().equals(user.getId());

        if (!isTenant && !isOwner) {
            throw new AccessDeniedException(
                    "You do not have access to this lease"
            );
        }

        LocalDate today = LocalDate.now();

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(
                today.lengthOfMonth()
        );

        Optional<Payment> payment =
                paymentRepository
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



}