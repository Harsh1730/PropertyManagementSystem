package com.harsh.propertymanagementsystem.review.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerReviewSummaryResponse {

    private Long ownerId;
    private String ownerName;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewResponse> reviews;
}
