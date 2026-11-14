package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAnswerOptionRequest(
        @NotBlank(message = "Текст варианта обязателен")
        @Size(max = 500, message = "Текст варианта не длиннее 500 символов")
        String text,
        @NotNull(message = "Нужно указать, правильный ли это вариант")
        Boolean correct
) {
}
