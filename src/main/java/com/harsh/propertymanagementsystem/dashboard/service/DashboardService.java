package com.harsh.propertymanagementsystem.dashboard.service;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.dashboard.dto.*;
import com.harsh.propertymanagementsystem.lease.entity.Lease;
import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import com.harsh.propertymanagementsystem.lease.repository.LeaseRepository;
import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceStatus;
import com.harsh.propertymanagementsystem.maintenance.repository.MaintenanceRepository;
import com.harsh.propertymanagementsystem.payment.entity.Payment;
import com.harsh.propertymanagementsystem.payment.entity.PaymentStatus;
import com.harsh.propertymanagementsystem.payment.repository.PaymentRepository;
import com.harsh.propertymanagementsystem.payment.service.PaymentService;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final PaymentService paymentService;

    public Object getDashboard() {
        User user = getCurrentUser();
        if (user.getRole() == Role.OWNER) {
            return getOwnerDashboard();
        } else if (user.getRole() == Role.TENANT) {
            return getTenantDashboard();
        }
        throw new AccessDeniedException("Unknown user role: " + user.getRole());
    }

    public OwnerDashboardResponse getOwnerDashboard() {
        User owner = getCurrentUser();
        if (owner.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only property owners can access the owner dashboard.");
        }

        long totalProperties = propertyRepository.findByOwnerId(owner.getId()).size();

        long occupiedProperties = propertyRepository
                .findByOwnerIdAndStatus(owner.getId(), PropertyStatus.OCCUPIED)
                .size();

        long availableProperties = propertyRepository
                .findByOwnerIdAndStatus(owner.getId(), PropertyStatus.AVAILABLE)
                .size();

        long activeLeases = leaseRepository
                .findByPropertyOwnerId(owner.getId())
                .stream()
                .filter(lease -> lease.getStatus() == LeaseStatus.ACTIVE)
                .count();

        BigDecimal totalRentCollected = paymentRepository
                .findByLeasePropertyOwnerId(owner.getId())
                .stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingMaintenanceRequests = maintenanceRepository
                .findByPropertyOwnerId(owner.getId())
                .stream()
                .filter(request ->
                        request.getStatus() == MaintenanceStatus.OPEN ||
                                request.getStatus() == MaintenanceStatus.IN_PROGRESS)
                .count();

        log.info("Generated owner dashboard for user {}", owner.getEmail());
        return new OwnerDashboardResponse(
                totalProperties,
                occupiedProperties,
                availableProperties,
                activeLeases,
                totalRentCollected,
                pendingMaintenanceRequests
        );
    }

    public TenantDashboardResponse getTenantDashboard() {
        User tenant = getCurrentUser();
        if (tenant.getRole() != Role.TENANT) {
            throw new AccessDeniedException("Only tenants can access the tenant dashboard.");
        }

        Lease activeLease = leaseRepository
                .findByTenantId(tenant.getId())
                .stream()
                .filter(lease -> lease.getStatus() == LeaseStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        if (activeLease == null) {
            log.info("Generated tenant dashboard for user {} (no active lease)", tenant.getEmail());
            return new TenantDashboardResponse(
                    null,
                    null,
                    null,
                    null,
                    BigDecimal.ZERO,
                    "NO_ACTIVE_LEASE",
                    null,
                    null,
                    null,
                    BigDecimal.ZERO,
                    0
            );
        }

        BigDecimal totalPayments = paymentRepository
                .findByLeaseTenantId(tenant.getId())
                .stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long openMaintenanceRequests = maintenanceRepository
                .findByTenantId(tenant.getId())
                .stream()
                .filter(request ->
                        request.getStatus() == MaintenanceStatus.OPEN ||
                                request.getStatus() == MaintenanceStatus.IN_PROGRESS)
                .count();

        String rentStatus = paymentService.getRentStatus(activeLease.getId()).status();

        Property property = activeLease.getProperty();

        log.info("Generated tenant dashboard for user {} with active lease {}", tenant.getEmail(), activeLease.getId());
        return new TenantDashboardResponse(
                activeLease.getId(),
                property != null ? property.getId() : null,
                property != null ? property.getPropertyName() : null,
                property != null ? property.getAddress() : null,
                activeLease.getMonthlyRent(),
                rentStatus,
                activeLease.getRentDueDay(),
                activeLease.getLeaseStartDate(),
                activeLease.getLeaseEndDate(),
                totalPayments,
                openMaintenanceRequests
        );
    }

    public List<OwnerRentedPropertyResponse> getOwnerRentedProperties() {
        User owner = getCurrentUser();
        if (owner.getRole() != Role.OWNER) {
            throw new AccessDeniedException("Only property owners can view rented properties.");
        }

        List<Property> ownerProperties = propertyRepository.findByOwnerId(owner.getId());
        List<Lease> allOwnerLeases = leaseRepository.findByPropertyOwnerId(owner.getId());

        List<OwnerRentedPropertyResponse> rentedProperties = new ArrayList<>();

        for (Property property : ownerProperties) {
            Optional<Lease> activeLeaseOpt = allOwnerLeases.stream()
                    .filter(l -> l.getProperty().getId().equals(property.getId()) && l.getStatus() == LeaseStatus.ACTIVE)
                    .findFirst();

            if (activeLeaseOpt.isEmpty() && property.getStatus() != PropertyStatus.OCCUPIED) {
                continue;
            }

            Lease activeLease = activeLeaseOpt.orElse(null);

            Long leaseId = activeLease != null ? activeLease.getId() : null;
            LocalDate startDate = activeLease != null ? activeLease.getLeaseStartDate() : null;
            LocalDate endDate = activeLease != null ? activeLease.getLeaseEndDate() : null;
            BigDecimal monthlyRent = activeLease != null ? activeLease.getMonthlyRent() : property.getRentAmount();
            BigDecimal leaseDeposit = activeLease != null ? activeLease.getSecurityDeposit() : property.getSecurityDeposit();
            Integer rentDueDay = activeLease != null ? activeLease.getRentDueDay() : null;
            LeaseStatus leaseStatus = activeLease != null ? activeLease.getStatus() : null;

            Long tenantId = (activeLease != null && activeLease.getTenant() != null) ? activeLease.getTenant().getId() : null;
            String tenantName = (activeLease != null && activeLease.getTenant() != null) ? activeLease.getTenant().getName() : null;
            String tenantEmail = (activeLease != null && activeLease.getTenant() != null) ? activeLease.getTenant().getEmail() : null;
            String tenantPhone = (activeLease != null && activeLease.getTenant() != null) ? activeLease.getTenant().getPhoneNumber() : null;

            String rentStatus = (activeLease != null) ? paymentService.getRentStatus(activeLease.getId()).status() : "N/A";

            BigDecimal totalRentCollected = (activeLease != null)
                    ? paymentRepository.findByLeaseId(activeLease.getId()).stream()
                        .filter(p -> p.getStatus() == PaymentStatus.PAID)
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            long openMaintenanceRequestsCount = maintenanceRepository.findByPropertyId(property.getId()).stream()
                    .filter(r -> r.getStatus() == MaintenanceStatus.OPEN || r.getStatus() == MaintenanceStatus.IN_PROGRESS)
                    .count();

            rentedProperties.add(OwnerRentedPropertyResponse.builder()
                    .propertyId(property.getId())
                    .propertyName(property.getPropertyName())
                    .description(property.getDescription())
                    .address(property.getAddress())
                    .city(property.getCity())
                    .state(property.getState())
                    .country(property.getCountry())
                    .postalCode(property.getPostalCode())
                    .propertyType(property.getPropertyType())
                    .status(property.getStatus())
                    .propertyRentAmount(property.getRentAmount())
                    .propertySecurityDeposit(property.getSecurityDeposit())
                    .totalUnits(property.getTotalUnits())
                    .propertyCreatedAt(property.getCreatedAt())
                    .leaseId(leaseId)
                    .leaseStartDate(startDate)
                    .leaseEndDate(endDate)
                    .monthlyRent(monthlyRent)
                    .leaseSecurityDeposit(leaseDeposit)
                    .rentDueDay(rentDueDay)
                    .leaseStatus(leaseStatus)
                    .tenantId(tenantId)
                    .tenantName(tenantName)
                    .tenantEmail(tenantEmail)
                    .tenantPhoneNumber(tenantPhone)
                    .rentStatus(rentStatus)
                    .totalRentCollected(totalRentCollected)
                    .openMaintenanceRequestsCount(openMaintenanceRequestsCount)
                    .build());
        }

        log.info("Found {} rented properties for owner {}", rentedProperties.size(), owner.getEmail());
        return rentedProperties;
    }

    public TenantLeaseOverviewResponse getTenantLeaseOverview() {
        User tenant = getCurrentUser();
        if (tenant.getRole() != Role.TENANT) {
            throw new AccessDeniedException("Only tenants can access tenant lease history.");
        }

        List<Lease> leases = leaseRepository.findByTenantId(tenant.getId());

        TenantLeaseDetailResponse currentLease = leases.stream()
                .filter(l -> l.getStatus() == LeaseStatus.ACTIVE)
                .findFirst()
                .map(this::mapToTenantLeaseDetail)
                .orElse(null);

        List<TenantLeaseDetailResponse> previousLeases = leases.stream()
                .filter(l -> l.getStatus() != LeaseStatus.ACTIVE)
                .sorted((a, b) -> {
                    if (a.getLeaseEndDate() == null || b.getLeaseEndDate() == null) return 0;
                    return b.getLeaseEndDate().compareTo(a.getLeaseEndDate());
                })
                .map(this::mapToTenantLeaseDetail)
                .toList();

        log.info("Fetched lease overview for tenant {}: 1 active, {} previous leases",
                tenant.getEmail(), previousLeases.size());

        return TenantLeaseOverviewResponse.builder()
                .currentLease(currentLease)
                .previousLeases(previousLeases)
                .totalLeases(leases.size())
                .build();
    }

    public List<TenantLeaseDetailResponse> getTenantPreviousLeases() {
        return getTenantLeaseOverview().getPreviousLeases();
    }

    public TenantLeaseDetailResponse getTenantCurrentLease() {
        TenantLeaseDetailResponse current = getTenantLeaseOverview().getCurrentLease();
        if (current == null) {
            throw new ResourceNotFoundException("No active lease found for the current tenant.");
        }
        return current;
    }

    private TenantLeaseDetailResponse mapToTenantLeaseDetail(Lease lease) {
        Property property = lease.getProperty();
        User owner = property != null ? property.getOwner() : null;

        String rentStatus;
        if (lease.getStatus() == LeaseStatus.ACTIVE) {
            rentStatus = paymentService.getRentStatus(lease.getId()).status();
        } else {
            rentStatus = lease.getStatus().name();
        }

        BigDecimal totalRentPaid = paymentRepository.findByLeaseId(lease.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long openMaintenance = (property != null)
                ? maintenanceRepository.findByTenantId(lease.getTenant().getId()).stream()
                    .filter(r -> r.getProperty() != null
                            && r.getProperty().getId().equals(property.getId())
                            && (r.getStatus() == MaintenanceStatus.OPEN || r.getStatus() == MaintenanceStatus.IN_PROGRESS))
                    .count()
                : 0;

        return TenantLeaseDetailResponse.builder()
                .leaseId(lease.getId())
                .leaseStartDate(lease.getLeaseStartDate())
                .leaseEndDate(lease.getLeaseEndDate())
                .monthlyRent(lease.getMonthlyRent())
                .securityDeposit(lease.getSecurityDeposit())
                .rentDueDay(lease.getRentDueDay())
                .status(lease.getStatus())
                .createdAt(lease.getCreatedAt())
                .propertyId(property != null ? property.getId() : null)
                .propertyName(property != null ? property.getPropertyName() : null)
                .propertyDescription(property != null ? property.getDescription() : null)
                .propertyAddress(property != null ? property.getAddress() : null)
                .propertyCity(property != null ? property.getCity() : null)
                .propertyState(property != null ? property.getState() : null)
                .propertyCountry(property != null ? property.getCountry() : null)
                .propertyPostalCode(property != null ? property.getPostalCode() : null)
                .propertyType(property != null ? property.getPropertyType() : null)
                .ownerId(owner != null ? owner.getId() : null)
                .ownerName(owner != null ? owner.getName() : null)
                .ownerEmail(owner != null ? owner.getEmail() : null)
                .ownerPhoneNumber(owner != null ? owner.getPhoneNumber() : null)
                .rentStatus(rentStatus)
                .totalRentPaid(totalRentPaid)
                .openMaintenanceRequestsCount(openMaintenance)
                .build();
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