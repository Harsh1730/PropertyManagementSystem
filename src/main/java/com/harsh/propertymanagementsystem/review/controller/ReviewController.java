package com.harsh.propertymanagementsystem.review.controller;

import com.harsh.propertymanagementsystem.review.dto.CreateReviewRequest;
import com.harsh.propertymanagementsystem.review.dto.OwnerReviewSummaryResponse;
import com.harsh.propertymanagementsystem.review.dto.PropertyReviewSummaryResponse;
import com.harsh.propertymanagementsystem.review.dto.ReviewResponse;
import com.harsh.propertymanagementsystem.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/properties/{propertyId}/reviews")
    public ResponseEntity<ReviewResponse> addPropertyReview(
            @PathVariable Long propertyId,
            @Valid @RequestBody CreateReviewRequest request) {
        log.info("Received request to add review for property #{}", propertyId);
        return ResponseEntity.ok(reviewService.addPropertyReview(propertyId, request));
    }

    @GetMapping("/properties/{propertyId}/reviews")
    public ResponseEntity<PropertyReviewSummaryResponse> getPropertyReviews(
            @PathVariable Long propertyId) {
        log.info("Received request to get reviews for property #{}", propertyId);
        return ResponseEntity.ok(reviewService.getPropertyReviews(propertyId));
    }

    @PostMapping("/owners/{ownerId}/reviews")
    public ResponseEntity<ReviewResponse> addOwnerReview(
            @PathVariable Long ownerId,
            @Valid @RequestBody CreateReviewRequest request) {
        log.info("Received request to add review for owner #{}", ownerId);
        return ResponseEntity.ok(reviewService.addOwnerReview(ownerId, request));
    }

    @GetMapping("/owners/{ownerId}/reviews")
    public ResponseEntity<OwnerReviewSummaryResponse> getOwnerReviews(
            @PathVariable Long ownerId) {
        log.info("Received request to get reviews for owner #{}", ownerId);
        return ResponseEntity.ok(reviewService.getOwnerReviews(ownerId));
    }
}
