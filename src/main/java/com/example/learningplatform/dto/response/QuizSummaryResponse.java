package com.example.learningplatform.dto.response;

public record QuizSummaryResponse(
        Long id,
        String title,
        Integer timeLimit
) {
}
