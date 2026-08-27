package com.harsh.propertymanagementsystem.maintenance.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.maintenance.dto.CreateMaintenanceRequest;
import com.harsh.propertymanagementsystem.maintenance.dto.MaintenanceResponse;
import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceRequest;
import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceStatus;
import com.harsh.propertymanagementsystem.maintenance.mapper.MaintenanceMapper;
import com.harsh.propertymanagementsystem.maintenance.repository.MaintenanceRepository;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final MaintenanceMapper maintenanceMapper;

    public MaintenanceResponse createRequest(CreateMaintenanceRequest request) {
        User tenant = getCurrentUser();

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + request.getPropertyId()));

        if (property.getStatus() != PropertyStatus.OCCUPIED) {
            throw new IllegalStateException("Maintenance request can only be created for an occupied property");
        }

        if (property.getOwner().getId().equals(tenant.getId())) {
            throw new AccessDeniedException("Owner cannot create a tenant maintenance request on own property");
        }

        MaintenanceRequest maintenanceRequest = maintenanceMapper.toEntity(request);
        maintenanceRequest.setProperty(property);
        maintenanceRequest.setTenant(tenant);
        maintenanceRequest.setStatus(MaintenanceStatus.OPEN);

        MaintenanceRequest saved = maintenanceRepository.save(maintenanceRequest);
        log.info("Created maintenance request {} for property {} by user {}", saved.getId(), property.getId(), tenant.getEmail());
        return maintenanceMapper.toResponse(saved);
    }

    public List<MaintenanceResponse> getMyRequests() {
        User tenant = getCurrentUser();
        return maintenanceRepository.findByTenantId(tenant.getId())
                .stream()
                .map(maintenanceMapper::toResponse)
                .toList();
    }

    public List<MaintenanceResponse> getOwnerRequests() {
        User owner = getCurrentUser();
        return maintenanceRepository.findByPropertyOwnerId(owner.getId())
                .stream()
                .map(maintenanceMapper::toResponse)
                .toList();
    }

    public MaintenanceResponse updateStatus(Long requestId, MaintenanceStatus status) {
        User owner = getCurrentUser();

        MaintenanceRequest request = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found with id: " + requestId));

        if (!request.getProperty().getOwner().getId().equals(owner.getId())) {
            log.warn("User {} unauthorized to update maintenance request {}", owner.getEmail(), requestId);
            throw new AccessDeniedException("You do not own the property associated with this maintenance request");
        }

        request.setStatus(status);
        MaintenanceRequest updated = maintenanceRepository.save(request);
        log.info("Updated maintenance request {} status to {}", requestId, status);
        return maintenanceMapper.toResponse(updated);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No authenticated user found");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}