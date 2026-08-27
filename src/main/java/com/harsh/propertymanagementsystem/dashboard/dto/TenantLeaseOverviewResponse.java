package com.harsh.propertymanagementsystem.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantLeaseOverviewResponse {
    private TenantLeaseDetailResponse currentLease;
    private List<TenantLeaseDetailResponse> previousLeases;
    private int totalLeases;
}
