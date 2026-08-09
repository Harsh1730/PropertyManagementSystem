package com.harsh.propertymanagementsystem.payment.entity;

import com.harsh.propertymanagementsystem.lease.entity.Lease;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lease_id", nullable = false)
    private Lease lease;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDate paymentDate;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionReference;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @CreationTimestamp
    private LocalDateTime createdAt;
}