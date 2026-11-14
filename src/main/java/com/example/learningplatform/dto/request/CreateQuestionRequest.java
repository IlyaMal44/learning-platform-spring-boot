package com.example.learningplatform.dto.request;

import com.example.learningplatform.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuestionRequest(
        @NotBlank(message = "Текст вопроса обязателен")
        @Size(max = 2000, message = "Текст вопроса не длиннее 2000 символов")
        String text,
        @NotNull(message = "Тип вопроса обязателен")
        QuestionType type,
        @NotEmpty(message = "У вопроса должно быть хотя бы два варианта")
        @Size(min = 2, message = "У вопроса должно быть хотя бы два варианта")
        @Valid
        List<CreateAnswerOptionRequest> options
) {
}
