package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSubmissionRequest(
        @NotNull(message = "Студент обязателен")
        Long studentId,
        @NotBlank(message = "Текст решения обязателен")
        @Size(max = 10000, message = "Текст решения не длиннее 10000 символов")
        String content
) {
}
