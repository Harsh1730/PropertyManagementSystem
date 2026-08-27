package com.harsh.propertymanagementsystem.review.repository;

import com.harsh.propertymanagementsystem.review.entity.OwnerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnerReviewRepository extends JpaRepository<OwnerReview, Long> {

    List<OwnerReview> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @Query("SELECT AVG(r.rating) FROM OwnerReview r WHERE r.owner.id = :ownerId")
    Double findAverageRatingByOwnerId(@Param("ownerId") Long ownerId);

    long countByOwnerId(Long ownerId);
}
