package com.harsh.propertymanagementsystem.maintenance.controller;

import com.harsh.propertymanagementsystem.maintenance.dto.CreateMaintenanceRequest;
import com.harsh.propertymanagementsystem.maintenance.dto.MaintenanceResponse;
import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceStatus;
import com.harsh.propertymanagementsystem.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    public ResponseEntity<MaintenanceResponse> createRequest(
            @Valid @RequestBody CreateMaintenanceRequest request) {
        log.info("Received request to create maintenance request for property {}", request.getPropertyId());
        return ResponseEntity.ok(maintenanceService.createRequest(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<MaintenanceResponse>> getMyRequests() {
        log.info("Received request to get my maintenance requests");
        return ResponseEntity.ok(maintenanceService.getMyRequests());
    }

    @GetMapping("/owner")
    public ResponseEntity<List<MaintenanceResponse>> getOwnerRequests() {
        log.info("Received request to get owner maintenance requests");
        return ResponseEntity.ok(maintenanceService.getOwnerRequests());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MaintenanceResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam MaintenanceStatus status) {
        log.info("Received request to update maintenance request {} to status {}", id, status);
        return ResponseEntity.ok(maintenanceService.updateStatus(id, status));
    }
}