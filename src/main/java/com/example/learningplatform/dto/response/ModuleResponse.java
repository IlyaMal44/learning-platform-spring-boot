package com.example.learningplatform.dto.response;

import java.util.List;

public record ModuleResponse(
        Long id,
        Long courseId,
        String title,
        int orderIndex,
        String description,
        List<LessonResponse> lessons,
        QuizSummaryResponse quiz
) {
}
