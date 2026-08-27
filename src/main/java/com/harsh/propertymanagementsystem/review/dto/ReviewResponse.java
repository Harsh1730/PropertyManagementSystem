package com.harsh.propertymanagementsystem.review.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String comment;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerEmail;
    private Long targetId;
    private LocalDateTime createdAt;
}
