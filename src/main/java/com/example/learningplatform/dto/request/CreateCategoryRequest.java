package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Название категории обязательно")
        @Size(max = 100, message = "Название категории не длиннее 100 символов")
        String name
) {
}
