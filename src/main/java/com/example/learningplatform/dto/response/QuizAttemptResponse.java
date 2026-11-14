package com.example.learningplatform.dto.response;

import java.time.LocalDateTime;

public record QuizAttemptResponse(
        Long id,
        Long quizId,
        Long studentId,
        String studentName,
        int score,
        int correctAnswers,
        int totalQuestions,
        LocalDateTime takenAt
) {
}
