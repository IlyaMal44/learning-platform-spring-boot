package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotNull(message = "Студент обязателен")
        Long studentId,
        @NotNull(message = "Оценка обязательна")
        @Min(value = 1, message = "Оценка не меньше 1")
        @Max(value = 5, message = "Оценка не больше 5")
        Integer rating,
        @Size(max = 2000, message = "Комментарий не длиннее 2000 символов")
        String comment
) {
}
