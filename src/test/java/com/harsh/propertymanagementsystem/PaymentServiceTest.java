package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.auth.entity.Role;
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
import com.harsh.propertymanagementsystem.payment.entity.PaymentMethod;
import com.harsh.propertymanagementsystem.payment.entity.PaymentStatus;
import com.harsh.propertymanagementsystem.payment.mapper.PaymentMapper;
import com.harsh.propertymanagementsystem.payment.repository.PaymentRepository;
import com.harsh.propertymanagementsystem.payment.service.PaymentService;
import com.harsh.propertymanagementsystem.property.entity.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private PaymentMapper paymentMapper = new PaymentMapper();

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User tenant;
    private User owner;
    private Lease activeLease;

    @BeforeEach
    void setUp() {
        tenant = User.builder()
                .id(2L)
                .email("tenant@test.com")
                .role(Role.TENANT)
                .build();

        owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .role(Role.OWNER)
                .build();

        Property property = Property.builder()
                .id(10L)
                .owner(owner)
                .build();

        activeLease = Lease.builder()
                .id(100L)
                .tenant(tenant)
                .property(property)
                .status(LeaseStatus.ACTIVE)
                .monthlyRent(new BigDecimal("1200.00"))
                .rentDueDay(10)
                .build();
    }

    @Test
    void testCreatePaymentSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("tenant@test.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("tenant@test.com")).thenReturn(Optional.of(tenant));
        when(leaseRepository.findById(100L)).thenReturn(Optional.of(activeLease));

        Payment savedPayment = Payment.builder()
                .id(500L)
                .lease(activeLease)
                .amount(new BigDecimal("1200.00"))
                .paymentDate(LocalDate.now())
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.PAID)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .leaseId(100L)
                .amount(new BigDecimal("1200.00"))
                .paymentDate(LocalDate.now())
                .paymentMethod(PaymentMethod.UPI)
                .build();

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1200.00"), response.amount());
        assertEquals(PaymentStatus.PAID, response.status());
    }

    @Test
    void testCreatePayment_NotTenant_ThrowsAccessDenied() {
        User otherTenant = User.builder().id(99L).email("other@test.com").build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("other@test.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherTenant));
        when(leaseRepository.findById(100L)).thenReturn(Optional.of(activeLease));

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .leaseId(100L)
                .amount(new BigDecimal("1200.00"))
                .paymentDate(LocalDate.now())
                .paymentMethod(PaymentMethod.UPI)
                .build();

        assertThrows(AccessDeniedException.class, () -> paymentService.createPayment(request));
    }

    @Test
    void testGetRentStatus_Paid() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("tenant@test.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("tenant@test.com")).thenReturn(Optional.of(tenant));
        when(leaseRepository.findById(100L)).thenReturn(Optional.of(activeLease));

        Payment payment = Payment.builder()
                .status(PaymentStatus.PAID)
                .amount(new BigDecimal("1200.00"))
                .build();

        when(paymentRepository.findFirstByLeaseIdAndPaymentDateBetweenOrderByPaymentDateDesc(
                eq(100L), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(Optional.of(payment));

        RentStatusResponse rentStatus = paymentService.getRentStatus(100L);

        assertNotNull(rentStatus);
        assertEquals("PAID", rentStatus.status());
    }
}
