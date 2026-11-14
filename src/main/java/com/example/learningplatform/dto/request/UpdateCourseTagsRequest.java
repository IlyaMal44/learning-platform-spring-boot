package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateCourseTagsRequest(
        @NotNull(message = "Список тегов обязателен")
        List<Long> tagIds
) {
}
