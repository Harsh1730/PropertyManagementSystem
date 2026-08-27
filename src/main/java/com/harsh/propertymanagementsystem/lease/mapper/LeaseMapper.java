package com.harsh.propertymanagementsystem.lease.mapper;

import com.harsh.propertymanagementsystem.lease.dto.CreateLeaseRequest;
import com.harsh.propertymanagementsystem.lease.dto.LeaseResponse;
import com.harsh.propertymanagementsystem.lease.entity.Lease;
import org.springframework.stereotype.Component;

@Component
public class LeaseMapper {

    public Lease toEntity(CreateLeaseRequest request) {
        Lease lease = new Lease();

        lease.setLeaseStartDate(request.getLeaseStartDate());
        lease.setLeaseEndDate(request.getLeaseEndDate());
        lease.setMonthlyRent(request.getMonthlyRent());
        lease.setSecurityDeposit(request.getSecurityDeposit());
        lease.setRentDueDay(request.getRentDueDay());

        return lease;
    }

    public LeaseResponse toResponse(Lease lease) {
        LeaseResponse response = new LeaseResponse();

        response.setId(lease.getId());
        response.setPropertyId(lease.getProperty().getId());
        response.setPropertyName(lease.getProperty().getPropertyName());
        response.setTenantId(lease.getTenant().getId());
        response.setTenantName(lease.getTenant().getName());
        response.setLeaseStartDate(lease.getLeaseStartDate());
        response.setLeaseEndDate(lease.getLeaseEndDate());
        response.setMonthlyRent(lease.getMonthlyRent());
        response.setSecurityDeposit(lease.getSecurityDeposit());
        response.setRentDueDay(lease.getRentDueDay());
        response.setStatus(lease.getStatus());
        response.setCreatedAt(lease.getCreatedAt());

        return response;
    }
}