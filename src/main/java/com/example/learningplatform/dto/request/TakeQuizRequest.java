package com.example.learningplatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TakeQuizRequest(
        @NotNull(message = "Студент обязателен")
        Long studentId,
        @NotEmpty(message = "Нужно передать ответы")
        @Valid
        List<QuizAnswerRequest> answers
) {
}
