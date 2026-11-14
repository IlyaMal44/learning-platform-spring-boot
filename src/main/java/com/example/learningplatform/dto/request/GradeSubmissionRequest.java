package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeSubmissionRequest(
        @NotNull(message = "Преподаватель обязателен")
        Long teacherId,
        @NotNull(message = "Оценка обязательна")
        @Min(value = 0, message = "Оценка не может быть отрицательной")
        Integer score,
        @Size(max = 4000, message = "Комментарий не длиннее 4000 символов")
        String feedback
) {
}
