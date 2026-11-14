package com.example.learningplatform.mapper;

import com.example.learningplatform.domain.Enrollment;
import com.example.learningplatform.dto.response.EnrollmentResponse;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus());
    }
}
