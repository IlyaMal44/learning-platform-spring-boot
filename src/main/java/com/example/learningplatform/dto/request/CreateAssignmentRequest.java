package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAssignmentRequest(
        @NotBlank(message = "Название задания обязательно")
        @Size(max = 200, message = "Название задания не длиннее 200 символов")
        String title,
        @Size(max = 5000, message = "Описание задания не длиннее 5000 символов")
        String description,
        LocalDate dueDate,
        @NotNull(message = "Максимальный балл обязателен")
        @Min(value = 1, message = "Максимальный балл должен быть больше нуля")
        Integer maxScore
) {
}
