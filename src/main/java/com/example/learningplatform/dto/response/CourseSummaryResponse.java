package com.example.learningplatform.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CourseSummaryResponse(
        Long id,
        String title,
        String description,
        String duration,
        LocalDate startDate,
        Long categoryId,
        String categoryName,
        Long teacherId,
        String teacherName,
        List<TagResponse> tags
) {
}
