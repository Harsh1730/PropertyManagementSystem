package com.harsh.propertymanagementsystem.maintenance.repository;

import com.harsh.propertymanagementsystem.maintenance.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRepository
        extends JpaRepository<MaintenanceRequest, Long> {

    List<MaintenanceRequest> findByTenantId(Long tenantId);

    List<MaintenanceRequest> findByPropertyOwnerId(Long ownerId);

    List<MaintenanceRequest> findByPropertyId(Long propertyId);
}