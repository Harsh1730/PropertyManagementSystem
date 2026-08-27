package com.harsh.propertymanagementsystem.review.mapper;

import com.harsh.propertymanagementsystem.review.dto.ReviewResponse;
import com.harsh.propertymanagementsystem.review.entity.OwnerReview;
import com.harsh.propertymanagementsystem.review.entity.PropertyReview;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(PropertyReview review) {
        if (review == null) return null;

        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewerId(review.getTenant() != null ? review.getTenant().getId() : null)
                .reviewerName(review.getTenant() != null ? review.getTenant().getName() : null)
                .reviewerEmail(review.getTenant() != null ? review.getTenant().getEmail() : null)
                .targetId(review.getProperty() != null ? review.getProperty().getId() : null)
                .createdAt(review.getCreatedAt())
                .build();
    }

    public ReviewResponse toResponse(OwnerReview review) {
        if (review == null) return null;

        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewerId(review.getTenant() != null ? review.getTenant().getId() : null)
                .reviewerName(review.getTenant() != null ? review.getTenant().getName() : null)
                .reviewerEmail(review.getTenant() != null ? review.getTenant().getEmail() : null)
                .targetId(review.getOwner() != null ? review.getOwner().getId() : null)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
