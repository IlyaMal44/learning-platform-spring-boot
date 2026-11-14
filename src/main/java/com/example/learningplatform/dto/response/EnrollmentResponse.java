package com.example.learningplatform.dto.response;

import com.example.learningplatform.enums.EnrollmentStatus;

import java.time.LocalDate;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long studentId,
        String studentName,
        LocalDate enrolledAt,
        EnrollmentStatus status
) {
}
