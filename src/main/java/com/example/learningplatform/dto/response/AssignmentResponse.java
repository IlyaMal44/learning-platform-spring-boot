package com.example.learningplatform.dto.response;

import java.time.LocalDate;

public record AssignmentResponse(
        Long id,
        Long lessonId,
        String title,
        String description,
        LocalDate dueDate,
        int maxScore
) {
}
