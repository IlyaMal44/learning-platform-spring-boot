package com.example.learningplatform.dto.request;

import com.example.learningplatform.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEnrollmentRequest(
        @NotNull(message = "Статус обязателен")
        EnrollmentStatus status
) {
}
