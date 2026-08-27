package com.harsh.propertymanagementsystem.review.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyReviewSummaryResponse {

    private Long propertyId;
    private String propertyName;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewResponse> reviews;
}
