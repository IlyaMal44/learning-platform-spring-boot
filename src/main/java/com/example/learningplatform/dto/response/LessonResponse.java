package com.example.learningplatform.dto.response;

import java.util.List;

public record LessonResponse(
        Long id,
        Long moduleId,
        String title,
        String content,
        String videoUrl,
        List<AssignmentResponse> assignments
) {
}
