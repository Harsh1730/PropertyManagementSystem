package com.harsh.propertymanagementsystem.property.dto;

import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private Long ownerId;
    private String ownerName;
    private Double ownerRating;
    private Double averageRating;
    private Integer totalReviews;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}


