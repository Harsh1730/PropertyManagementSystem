package com.harsh.propertymanagementsystem.review.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import com.harsh.propertymanagementsystem.review.dto.CreateReviewRequest;
import com.harsh.propertymanagementsystem.review.dto.OwnerReviewSummaryResponse;
import com.harsh.propertymanagementsystem.review.dto.PropertyReviewSummaryResponse;
import com.harsh.propertymanagementsystem.review.dto.ReviewResponse;
import com.harsh.propertymanagementsystem.review.entity.OwnerReview;
import com.harsh.propertymanagementsystem.review.entity.PropertyReview;
import com.harsh.propertymanagementsystem.review.mapper.ReviewMapper;
import com.harsh.propertymanagementsystem.review.repository.OwnerReviewRepository;
import com.harsh.propertymanagementsystem.review.repository.PropertyReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final PropertyReviewRepository propertyReviewRepository;
    private final OwnerReviewRepository ownerReviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse addPropertyReview(Long propertyId, CreateReviewRequest request) {
        User tenant = getCurrentUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));

        PropertyReview review = PropertyReview.builder()
                .property(property)
                .tenant(tenant)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .build();

        PropertyReview saved = propertyReviewRepository.save(review);
        log.info("Tenant {} submitted {} star review for property #{}", tenant.getEmail(), request.getRating(), propertyId);
        return reviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PropertyReviewSummaryResponse getPropertyReviews(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));

        List<PropertyReview> reviews = propertyReviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
        Double avg = propertyReviewRepository.findAverageRatingByPropertyId(propertyId);
        Double roundedAvg = avg != null ? roundToOneDecimal(avg) : null;

        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();

        return PropertyReviewSummaryResponse.builder()
                .propertyId(property.getId())
                .propertyName(property.getPropertyName())
                .averageRating(roundedAvg)
                .totalReviews(reviews.size())
                .reviews(reviewResponses)
                .build();
    }

    @Transactional
    public ReviewResponse addOwnerReview(Long ownerId, CreateReviewRequest request) {
        User tenant = getCurrentUser();

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + ownerId));

        if (owner.getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("Users cannot review themselves");
        }

        OwnerReview review = OwnerReview.builder()
                .owner(owner)
                .tenant(tenant)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .build();

        OwnerReview saved = ownerReviewRepository.save(review);
        log.info("Tenant {} submitted {} star review for owner #{}", tenant.getEmail(), request.getRating(), ownerId);
        return reviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OwnerReviewSummaryResponse getOwnerReviews(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with ID: " + ownerId));

        List<OwnerReview> reviews = ownerReviewRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        Double avg = ownerReviewRepository.findAverageRatingByOwnerId(ownerId);
        Double roundedAvg = avg != null ? roundToOneDecimal(avg) : null;

        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(reviewMapper::toResponse)
                .toList();

        return OwnerReviewSummaryResponse.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getName())
                .averageRating(roundedAvg)
                .totalReviews(reviews.size())
                .reviews(reviewResponses)
                .build();
    }

    public Double getAveragePropertyRating(Long propertyId) {
        Double avg = propertyReviewRepository.findAverageRatingByPropertyId(propertyId);
        return avg != null ? roundToOneDecimal(avg) : null;
    }

    public int getPropertyReviewCount(Long propertyId) {
        return (int) propertyReviewRepository.countByPropertyId(propertyId);
    }

    public Double getAverageOwnerRating(Long ownerId) {
        Double avg = ownerReviewRepository.findAverageRatingByOwnerId(ownerId);
        return avg != null ? roundToOneDecimal(avg) : null;
    }

    private Double roundToOneDecimal(Double val) {
        return BigDecimal.valueOf(val)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
