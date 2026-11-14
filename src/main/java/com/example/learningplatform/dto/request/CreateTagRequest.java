package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank(message = "Название тега обязательно")
        @Size(max = 50, message = "Название тега не длиннее 50 символов")
        String name
) {
}
