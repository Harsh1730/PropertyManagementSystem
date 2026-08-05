package com.harsh.propertymanagementsystem.property.dto;

import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@ AllArgsConstructor
public class PropertyResponse {
    private Long id;
    private String propertyName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private PropertyType propertyType;
    private PropertyStatus status;
    private BigDecimal rentAmount;
    private BigDecimal securityDeposit;
    private Integer totalUnits;
    private LocalDateTime createdAt;
}
