package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.dashboard.controller.DashboardController;
import com.harsh.propertymanagementsystem.dashboard.dto.OwnerDashboardResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.OwnerRentedPropertyResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.TenantDashboardResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.TenantLeaseDetailResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.TenantLeaseOverviewResponse;
import com.harsh.propertymanagementsystem.dashboard.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void testGetDashboard() {
        OwnerDashboardResponse ownerResp = new OwnerDashboardResponse(5, 3, 2, 3, BigDecimal.valueOf(5000), 1);
        when(dashboardService.getDashboard()).thenReturn(ownerResp);

        ResponseEntity<Object> response = dashboardController.getDashboard();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(ownerResp, response.getBody());
    }

    @Test
    void testGetOwnerDashboard() {
        OwnerDashboardResponse ownerResp = new OwnerDashboardResponse(5, 3, 2, 3, BigDecimal.valueOf(5000), 1);
        when(dashboardService.getOwnerDashboard()).thenReturn(ownerResp);

        ResponseEntity<OwnerDashboardResponse> response = dashboardController.getOwnerDashboard();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(5, response.getBody().totalProperties());
    }

    @Test
    void testGetTenantDashboard() {
        TenantDashboardResponse tenantResp = new TenantDashboardResponse(
                1L, 10L, "Sunset Villa", "123 Ocean Drive",
                BigDecimal.valueOf(1200), "PAID", 5,
                LocalDate.now(), LocalDate.now().plusMonths(6),
                BigDecimal.valueOf(1200), 0
        );
        when(dashboardService.getTenantDashboard()).thenReturn(tenantResp);

        ResponseEntity<TenantDashboardResponse> response = dashboardController.getTenantDashboard();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().activeLeaseId());
    }

    @Test
    void testGetOwnerRentedProperties() {
        OwnerRentedPropertyResponse prop = OwnerRentedPropertyResponse.builder()
                .propertyId(10L)
                .propertyName("Sunset Villa")
                .build();
        when(dashboardService.getOwnerRentedProperties()).thenReturn(List.of(prop));

        ResponseEntity<List<OwnerRentedPropertyResponse>> response = dashboardController.getOwnerRentedProperties();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).getPropertyId());
    }

    @Test
    void testGetTenantLeaseOverview() {
        TenantLeaseOverviewResponse overview = TenantLeaseOverviewResponse.builder()
                .totalLeases(2)
                .build();
        when(dashboardService.getTenantLeaseOverview()).thenReturn(overview);

        ResponseEntity<TenantLeaseOverviewResponse> response = dashboardController.getTenantLeaseOverview();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getTotalLeases());
    }

    @Test
    void testGetTenantCurrentLease() {
        TenantLeaseDetailResponse detail = TenantLeaseDetailResponse.builder()
                .leaseId(100L)
                .build();
        when(dashboardService.getTenantCurrentLease()).thenReturn(detail);

        ResponseEntity<TenantLeaseDetailResponse> response = dashboardController.getTenantCurrentLease();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(100L, response.getBody().getLeaseId());
    }

    @Test
    void testGetTenantPreviousLeases() {
        TenantLeaseDetailResponse detail = TenantLeaseDetailResponse.builder()
                .leaseId(90L)
                .build();
        when(dashboardService.getTenantPreviousLeases()).thenReturn(List.of(detail));

        ResponseEntity<List<TenantLeaseDetailResponse>> response = dashboardController.getTenantPreviousLeases();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(90L, response.getBody().get(0).getLeaseId());
    }
}
