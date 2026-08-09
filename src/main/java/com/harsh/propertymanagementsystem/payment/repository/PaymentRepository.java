package com.harsh.propertymanagementsystem.payment.repository;

import com.harsh.propertymanagementsystem.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByLeaseTenantId(Long tenantId);

    List<Payment> findByLeasePropertyOwnerId(Long ownerId);

    List<Payment> findByLeaseId(Long leaseId);

    Optional<Payment> findFirstByLeaseIdAndPaymentDateBetweenOrderByPaymentDateDesc(
            Long leaseId,
            LocalDate startDate,
            LocalDate endDate
    );
}