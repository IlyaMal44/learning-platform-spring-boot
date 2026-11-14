package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuizRequest(
        @NotBlank(message = "Название теста обязательно")
        @Size(max = 200, message = "Название теста не длиннее 200 символов")
        String title,
        @Min(value = 1, message = "Лимит времени должен быть больше нуля")
        Integer timeLimit
) {
}
