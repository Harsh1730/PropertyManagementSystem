package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.lease.dto.CreateLeaseRequest;
import com.harsh.propertymanagementsystem.lease.dto.LeaseResponse;
import com.harsh.propertymanagementsystem.lease.entity.Lease;
import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import com.harsh.propertymanagementsystem.lease.mapper.LeaseMapper;
import com.harsh.propertymanagementsystem.lease.repository.LeaseRepository;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import com.harsh.propertymanagementsystem.lease.service.LeaseService;
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
class LeaseServiceTest {

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private LeaseMapper leaseMapper = new LeaseMapper();

    @InjectMocks
    private LeaseService leaseService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User owner;
    private User tenant;
    private Property property;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .role(Role.OWNER)
                .build();

        tenant = User.builder()
                .id(2L)
                .email("tenant@test.com")
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.TENANT)
                .build();

        property = Property.builder()
                .id(10L)
                .propertyName("Sunset Villa")
                .status(PropertyStatus.AVAILABLE)
                .owner(owner)
                .build();
    }

    @Test
    void testCreateLeaseSuccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("owner@test.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tenant));

        Lease savedLease = Lease.builder()
                .id(100L)
                .property(property)
                .tenant(tenant)
                .leaseStartDate(LocalDate.now())
                .leaseEndDate(LocalDate.now().plusMonths(6))
                .monthlyRent(new BigDecimal("1000"))
                .securityDeposit(new BigDecimal("1000"))
                .rentDueDay(5)
                .status(LeaseStatus.ACTIVE)
                .build();

        when(leaseRepository.save(any(Lease.class))).thenReturn(savedLease);

        CreateLeaseRequest request = CreateLeaseRequest.builder()
                .propertyId(10L)
                .tenantId(2L)
                .leaseStartDate(LocalDate.now())
                .leaseEndDate(LocalDate.now().plusMonths(6))
                .monthlyRent(new BigDecimal("1000"))
                .securityDeposit(new BigDecimal("1000"))
                .rentDueDay(5)
                .build();

        LeaseResponse response = leaseService.createLease(request);

        assertNotNull(response);
        assertEquals(LeaseStatus.ACTIVE, response.getStatus());
        assertEquals(PropertyStatus.OCCUPIED, property.getStatus());
        verify(propertyRepository).save(property);
    }

    @Test
    void testCreateLease_NotOwner_ThrowsAccessDenied() {
        User otherUser = User.builder().id(99L).email("other@test.com").role(Role.OWNER).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("other@test.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

        CreateLeaseRequest request = CreateLeaseRequest.builder()
                .propertyId(10L)
                .tenantId(2L)
                .leaseStartDate(LocalDate.now())
                .leaseEndDate(LocalDate.now().plusMonths(6))
                .monthlyRent(new BigDecimal("1000"))
                .securityDeposit(new BigDecimal("1000"))
                .rentDueDay(5)
                .build();

        assertThrows(AccessDeniedException.class, () -> leaseService.createLease(request));
    }
}
