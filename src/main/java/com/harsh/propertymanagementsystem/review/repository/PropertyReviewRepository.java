package com.harsh.propertymanagementsystem.review.repository;

import com.harsh.propertymanagementsystem.review.entity.PropertyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyReviewRepository extends JpaRepository<PropertyReview, Long> {

    List<PropertyReview> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    @Query("SELECT AVG(r.rating) FROM PropertyReview r WHERE r.property.id = :propertyId")
    Double findAverageRatingByPropertyId(@Param("propertyId") Long propertyId);

    long countByPropertyId(Long propertyId);
}
