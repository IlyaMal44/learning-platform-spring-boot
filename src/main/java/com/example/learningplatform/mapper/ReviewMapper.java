package com.example.learningplatform.mapper;

import com.example.learningplatform.domain.CourseReview;
import com.example.learningplatform.dto.response.ReviewResponse;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(CourseReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getCourse().getId(),
                review.getStudent().getId(),
                review.getStudent().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}
