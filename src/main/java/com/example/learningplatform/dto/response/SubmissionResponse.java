package com.example.learningplatform.dto.response;

import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Long studentId,
        String studentName,
        LocalDateTime submittedAt,
        String content,
        Integer score,
        String feedback
) {
}
