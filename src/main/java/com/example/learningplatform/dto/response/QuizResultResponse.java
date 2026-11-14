package com.example.learningplatform.dto.response;

import java.time.LocalDateTime;

public record QuizResultResponse(
        Long id,
        Long quizId,
        String quizTitle,
        Long studentId,
        String studentName,
        int score,
        LocalDateTime takenAt
) {
}
