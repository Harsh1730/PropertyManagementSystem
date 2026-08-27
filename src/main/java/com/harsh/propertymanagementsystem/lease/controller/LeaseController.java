package com.harsh.propertymanagementsystem.lease.controller;

import com.harsh.propertymanagementsystem.dashboard.dto.TenantLeaseDetailResponse;
import com.harsh.propertymanagementsystem.dashboard.dto.TenantLeaseOverviewResponse;
import com.harsh.propertymanagementsystem.dashboard.service.DashboardService;
import com.harsh.propertymanagementsystem.lease.dto.CreateLeaseRequest;
import com.harsh.propertymanagementsystem.lease.dto.LeaseResponse;
import com.harsh.propertymanagementsystem.lease.service.LeaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/leases")
public class LeaseController {

    private final LeaseService leaseService;
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<List<LeaseResponse>> getMyLeases() {
        log.info("Received request to get my leases");
        return ResponseEntity.ok(leaseService.getMyLeases());
    }

    @PostMapping
    public ResponseEntity<LeaseResponse> createLease(
            @Valid @RequestBody CreateLeaseRequest request) {
        log.info("Received request to create lease for property {}", request.getPropertyId());
        return ResponseEntity.ok(leaseService.createLease(request));
    }

    @GetMapping("/current")
    public ResponseEntity<TenantLeaseDetailResponse> getCurrentLease() {
        log.info("Received request to get current active lease");
        return ResponseEntity.ok(dashboardService.getTenantCurrentLease());
    }

    @GetMapping("/previous")
    public ResponseEntity<List<TenantLeaseDetailResponse>> getPreviousLeases() {
        log.info("Received request to get previous leases");
        return ResponseEntity.ok(dashboardService.getTenantPreviousLeases());
    }

    @GetMapping("/history")
    public ResponseEntity<TenantLeaseOverviewResponse> getLeaseHistory() {
        log.info("Received request to get lease history overview");
        return ResponseEntity.ok(dashboardService.getTenantLeaseOverview());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaseResponse> getLease(@PathVariable Long id) {
        log.info("Received request to get lease {}", id);
        return ResponseEntity.ok(leaseService.getLease(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> terminateLease(@PathVariable Long id) {
        log.info("Received request to terminate lease {}", id);
        leaseService.terminateLease(id);
        return ResponseEntity.ok(Map.of("message", "Lease terminated successfully"));
    }
}