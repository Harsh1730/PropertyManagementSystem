package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.dashboard.dto.*;
import com.harsh.propertymanagementsystem.dashboard.service.DashboardService;
import com.harsh.propertymanagementsystem.lease.entity.Lease;
import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import com.harsh.propertymanagementsystem.lease.repository.LeaseRepository;
import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceRequest;
import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceStatus;
import com.harsh.propertymanagementsystem.maintenance.repository.MaintenanceRepository;
import com.harsh.propertymanagementsystem.payment.dto.RentStatusResponse;
import com.harsh.propertymanagementsystem.payment.entity.Payment;
import com.harsh.propertymanagementsystem.payment.entity.PaymentStatus;
import com.harsh.propertymanagementsystem.payment.repository.PaymentRepository;
import com.harsh.propertymanagementsystem.payment.service.PaymentService;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyType;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardService dashboardService;

    private User owner;
    private User tenant;
    private Property property;
    private Lease activeLease;
    private Lease expiredLease;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .firstName("John")
                .lastName("Owner")
                .phoneNumber("1234567890")
                .role(Role.OWNER)
                .build();

        tenant = User.builder()
                .id(2L)
                .email("tenant@test.com")
                .firstName("Jane")
                .lastName("Tenant")
                .phoneNumber("0987654321")
                .role(Role.TENANT)
                .build();

        property = Property.builder()
                .id(10L)
                .propertyName("Sunset Villa")
                .description("Luxury villa")
                .address("123 Ocean Drive")
                .city("Miami")
                .state("FL")
                .country("USA")
                .postalCode("33101")
                .propertyType(PropertyType.FLAT)
                .status(PropertyStatus.OCCUPIED)
                .rentAmount(new BigDecimal("2500"))
                .securityDeposit(new BigDecimal("2500"))
                .totalUnits(1)
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        activeLease = Lease.builder()
                .id(100L)
                .property(property)
                .tenant(tenant)
                .leaseStartDate(LocalDate.now().minusMonths(2))
                .leaseEndDate(LocalDate.now().plusMonths(10))
                .monthlyRent(new BigDecimal("2500"))
                .securityDeposit(new BigDecimal("2500"))
                .rentDueDay(5)
                .status(LeaseStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusMonths(2))
                .build();

        expiredLease = Lease.builder()
                .id(99L)
                .property(property)
                .tenant(tenant)
                .leaseStartDate(LocalDate.now().minusYears(2))
                .leaseEndDate(LocalDate.now().minusYears(1))
                .monthlyRent(new BigDecimal("2300"))
                .securityDeposit(new BigDecimal("2300"))
                .rentDueDay(1)
                .status(LeaseStatus.EXPIRED)
                .createdAt(LocalDateTime.now().minusYears(2))
                .build();
    }

    private void mockUser(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void testGetDashboard_RoutesToOwnerDashboard() {
        mockUser(owner);
        when(propertyRepository.findByOwnerId(1L)).thenReturn(List.of(property));
        when(propertyRepository.findByOwnerIdAndStatus(1L, PropertyStatus.OCCUPIED)).thenReturn(List.of(property));
        when(propertyRepository.findByOwnerIdAndStatus(1L, PropertyStatus.AVAILABLE)).thenReturn(List.of());
        when(leaseRepository.findByPropertyOwnerId(1L)).thenReturn(List.of(activeLease));
        when(paymentRepository.findByLeasePropertyOwnerId(1L)).thenReturn(List.of());
        when(maintenanceRepository.findByPropertyOwnerId(1L)).thenReturn(List.of());

        Object result = dashboardService.getDashboard();

        assertTrue(result instanceof OwnerDashboardResponse);
        OwnerDashboardResponse response = (OwnerDashboardResponse) result;
        assertEquals(1, response.totalProperties());
        assertEquals(1, response.occupiedProperties());
        assertEquals(0, response.availableProperties());
        assertEquals(1, response.activeLeases());
    }

    @Test
    void testGetDashboard_RoutesToTenantDashboard() {
        mockUser(tenant);
        when(leaseRepository.findByTenantId(2L)).thenReturn(List.of(activeLease));
        when(paymentRepository.findByLeaseTenantId(2L)).thenReturn(List.of());
        when(maintenanceRepository.findByTenantId(2L)).thenReturn(List.of());
        when(paymentService.getRentStatus(100L)).thenReturn(new RentStatusResponse(100L, new BigDecimal("2500"), 5, "PAID"));

        Object result = dashboardService.getDashboard();

        assertTrue(result instanceof TenantDashboardResponse);
        TenantDashboardResponse response = (TenantDashboardResponse) result;
        assertEquals(100L, response.activeLeaseId());
        assertEquals("Sunset Villa", response.propertyName());
        assertEquals("PAID", response.rentStatus());
    }

    @Test
    void testGetOwnerDashboard_TenantAccess_ThrowsAccessDenied() {
        mockUser(tenant);

        assertThrows(AccessDeniedException.class, () -> dashboardService.getOwnerDashboard());
    }

    @Test
    void testGetTenantDashboard_OwnerAccess_ThrowsAccessDenied() {
        mockUser(owner);

        assertThrows(AccessDeniedException.class, () -> dashboardService.getTenantDashboard());
    }

    @Test
    void testGetTenantDashboard_NoActiveLease() {
        mockUser(tenant);
        when(leaseRepository.findByTenantId(2L)).thenReturn(List.of(expiredLease));

        TenantDashboardResponse response = dashboardService.getTenantDashboard();

        assertNotNull(response);
        assertNull(response.activeLeaseId());
        assertEquals("NO_ACTIVE_LEASE", response.rentStatus());
        assertEquals(BigDecimal.ZERO, response.monthlyRent());
    }

    @Test
    void testGetOwnerRentedProperties_Success() {
        mockUser(owner);
        when(propertyRepository.findByOwnerId(1L)).thenReturn(List.of(property));
        when(leaseRepository.findByPropertyOwnerId(1L)).thenReturn(List.of(activeLease));
        when(paymentService.getRentStatus(100L)).thenReturn(new RentStatusResponse(100L, new BigDecimal("2500"), 5, "PAID"));

        Payment paidPayment = Payment.builder()
                .id(50L)
                .amount(new BigDecimal("2500"))
                .status(PaymentStatus.PAID)
                .build();
        when(paymentRepository.findByLeaseId(100L)).thenReturn(List.of(paidPayment));

        MaintenanceRequest openReq = MaintenanceRequest.builder()
                .id(10L)
                .status(MaintenanceStatus.OPEN)
                .build();
        when(maintenanceRepository.findByPropertyId(10L)).thenReturn(List.of(openReq));

        List<OwnerRentedPropertyResponse> result = dashboardService.getOwnerRentedProperties();

        assertNotNull(result);
        assertEquals(1, result.size());
        OwnerRentedPropertyResponse item = result.get(0);
        assertEquals(10L, item.getPropertyId());
        assertEquals("Sunset Villa", item.getPropertyName());
        assertEquals("Miami", item.getCity());
        assertEquals(100L, item.getLeaseId());
        assertEquals(2L, item.getTenantId());
        assertEquals("Jane Tenant", item.getTenantName());
        assertEquals("PAID", item.getRentStatus());
        assertEquals(new BigDecimal("2500"), item.getTotalRentCollected());
        assertEquals(1, item.getOpenMaintenanceRequestsCount());
    }

    @Test
    void testGetTenantLeaseOverview_Success() {
        mockUser(tenant);
        when(leaseRepository.findByTenantId(2L)).thenReturn(List.of(activeLease, expiredLease));
        when(paymentService.getRentStatus(100L)).thenReturn(new RentStatusResponse(100L, new BigDecimal("2500"), 5, "PAID"));
        when(paymentRepository.findByLeaseId(100L)).thenReturn(List.of());
        when(paymentRepository.findByLeaseId(99L)).thenReturn(List.of());
        when(maintenanceRepository.findByTenantId(2L)).thenReturn(List.of());

        TenantLeaseOverviewResponse overview = dashboardService.getTenantLeaseOverview();

        assertNotNull(overview);
        assertEquals(2, overview.getTotalLeases());
        assertNotNull(overview.getCurrentLease());
        assertEquals(100L, overview.getCurrentLease().getLeaseId());
        assertEquals(1, overview.getPreviousLeases().size());
        assertEquals(99L, overview.getPreviousLeases().get(0).getLeaseId());
        assertEquals("Sunset Villa", overview.getPreviousLeases().get(0).getPropertyName());
        assertEquals("John Owner", overview.getPreviousLeases().get(0).getOwnerName());
    }

    @Test
    void testGetTenantCurrentLease_NotFound_ThrowsResourceNotFound() {
        mockUser(tenant);
        when(leaseRepository.findByTenantId(2L)).thenReturn(List.of(expiredLease));
        when(paymentRepository.findByLeaseId(99L)).thenReturn(List.of());
        when(maintenanceRepository.findByTenantId(2L)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> dashboardService.getTenantCurrentLease());
    }
}
