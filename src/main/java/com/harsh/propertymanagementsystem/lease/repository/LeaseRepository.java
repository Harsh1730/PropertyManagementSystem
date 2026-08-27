package com.harsh.propertymanagementsystem.lease.repository;

import com.harsh.propertymanagementsystem.lease.entity.Lease;
import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, Long> {
    List<Lease> findByPropertyOwnerId(Long ownerId);
    List<Lease> findByTenantId(Long tenantId);
    List<Lease> findByTenantIdAndStatus(Long tenantId, LeaseStatus status);
    List<Lease> findByPropertyOwnerIdAndStatus(Long ownerId, LeaseStatus status);
    List<Lease> findByPropertyId(Long propertyId);
    Optional<Lease> findFirstByPropertyIdAndStatus(Long propertyId, LeaseStatus status);
}