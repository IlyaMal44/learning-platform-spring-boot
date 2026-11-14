package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuizAnswerRequest(
        @NotNull(message = "Вопрос обязателен")
        Long questionId,
        @NotEmpty(message = "Нужно выбрать хотя бы один вариант")
        List<Long> optionIds
) {
}
