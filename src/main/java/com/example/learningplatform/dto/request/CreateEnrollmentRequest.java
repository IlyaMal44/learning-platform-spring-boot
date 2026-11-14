package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateEnrollmentRequest(
        @NotNull(message = "Студент обязателен")
        Long studentId
) {
}
