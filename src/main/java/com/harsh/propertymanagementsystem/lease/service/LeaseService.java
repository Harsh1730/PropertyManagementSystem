package com.harsh.propertymanagementsystem.lease.service;

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
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final LeaseMapper leaseMapper;

    public LeaseResponse createLease(CreateLeaseRequest request) {
        User owner = getCurrentUser();

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + request.getPropertyId()));

        if (!property.getOwner().getId().equals(owner.getId())) {
            log.warn("User {} attempted to create lease for unowned property {}", owner.getEmail(), property.getId());
            throw new AccessDeniedException("You do not own this property.");
        }

        User tenant = userRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + request.getTenantId()));

        if (tenant.getRole() != Role.TENANT) {
            throw new IllegalArgumentException("Selected user is not a tenant.");
        }

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new IllegalStateException("Property is not available for lease (current status: " + property.getStatus() + ")");
        }

        if (!request.getLeaseEndDate().isAfter(request.getLeaseStartDate())) {
            throw new IllegalArgumentException("Lease end date must be after lease start date");
        }

        Lease lease = leaseMapper.toEntity(request);
        lease.setProperty(property);
        lease.setTenant(tenant);
        lease.setStatus(LeaseStatus.ACTIVE);

        Lease savedLease = leaseRepository.save(lease);

        property.setStatus(PropertyStatus.OCCUPIED);
        propertyRepository.save(property);

        log.info("Successfully created lease {} for property {} and tenant {}", savedLease.getId(), property.getId(), tenant.getId());
        return leaseMapper.toResponse(savedLease);
    }

    public LeaseResponse getLease(Long id) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + id));

        User user = getCurrentUser();

        boolean isOwner = lease.getProperty().getOwner().getId().equals(user.getId());
        boolean isTenant = lease.getTenant().getId().equals(user.getId());

        if (!isOwner && !isTenant) {
            log.warn("Access denied for user {} attempting to view lease {}", user.getEmail(), id);
            throw new AccessDeniedException("You do not have access to this lease");
        }

        return leaseMapper.toResponse(lease);
    }

    public List<LeaseResponse> getMyLeases() {
        User user = getCurrentUser();

        List<Lease> leases;
        if (user.getRole() == Role.TENANT) {
            leases = leaseRepository.findByTenantId(user.getId());
        } else {
            leases = leaseRepository.findByPropertyOwnerId(user.getId());
        }

        return leases.stream()
                .map(leaseMapper::toResponse)
                .toList();
    }

    public void terminateLease(Long leaseId) {
        User owner = getCurrentUser();

        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Lease not found with id: " + leaseId));

        if (!lease.getProperty().getOwner().getId().equals(owner.getId())) {
            log.warn("User {} unauthorized to terminate lease {}", owner.getEmail(), leaseId);
            throw new AccessDeniedException("You do not own this property.");
        }

        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new IllegalStateException("Lease is not active (current status: " + lease.getStatus() + ").");
        }

        lease.setStatus(LeaseStatus.TERMINATED);
        lease.getProperty().setStatus(PropertyStatus.AVAILABLE);

        propertyRepository.save(lease.getProperty());
        leaseRepository.save(lease);

        log.info("Terminated lease {} for property {}", leaseId, lease.getProperty().getId());
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