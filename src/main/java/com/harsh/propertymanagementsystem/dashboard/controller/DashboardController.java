package com.harsh.propertymanagementsystem.dashboard.controller;

import com.harsh.propertymanagementsystem.dashboard.dto.OwnerDashboardResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.OwnerRentedPropertyResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.TenantDashboardResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.TenantLeaseDetailResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.TenantLeaseOverviewResponse;
import com.harsh.propertymanagementsystem.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<Object> getDashboard() {
        log.info("Received request for role-based dashboard");
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/owner")
    public ResponseEntity<OwnerDashboardResponse> getOwnerDashboard() {
        log.info("Received request for owner dashboard");
        return ResponseEntity.ok(dashboardService.getOwnerDashboard());
    }

    @GetMapping("/tenant")
    public ResponseEntity<TenantDashboardResponse> getTenantDashboard() {
        log.info("Received request for tenant dashboard");
        return ResponseEntity.ok(dashboardService.getTenantDashboard());
    }

    @GetMapping("/owner/rented-properties")
    public ResponseEntity<List<OwnerRentedPropertyResponse>> getOwnerRentedProperties() {
        log.info("Received request for owner rented properties");
        return ResponseEntity.ok(dashboardService.getOwnerRentedProperties());
    }

    @GetMapping("/tenant/leases")
    public ResponseEntity<TenantLeaseOverviewResponse> getTenantLeaseOverview() {
        log.info("Received request for tenant lease overview");
        return ResponseEntity.ok(dashboardService.getTenantLeaseOverview());
    }

    @GetMapping("/tenant/current-lease")
    public ResponseEntity<TenantLeaseDetailResponse> getTenantCurrentLease() {
        log.info("Received request for tenant current lease");
        return ResponseEntity.ok(dashboardService.getTenantCurrentLease());
    }

    @GetMapping("/tenant/previous-leases")
    public ResponseEntity<List<TenantLeaseDetailResponse>> getTenantPreviousLeases() {
        log.info("Received request for tenant previous leases");
        return ResponseEntity.ok(dashboardService.getTenantPreviousLeases());
    }
}