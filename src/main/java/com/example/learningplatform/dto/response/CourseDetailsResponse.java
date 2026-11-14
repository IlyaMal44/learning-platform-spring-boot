package com.example.learningplatform.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CourseDetailsResponse(
        Long id,
        String title,
        String description,
        String duration,
        LocalDate startDate,
        CategoryResponse category,
        UserSummaryResponse teacher,
        List<TagResponse> tags,
        List<ModuleResponse> modules
) {
}
