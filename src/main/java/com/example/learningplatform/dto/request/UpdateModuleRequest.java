package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateModuleRequest(
        @NotBlank(message = "Название модуля обязательно")
        @Size(max = 200, message = "Название модуля не длиннее 200 символов")
        String title,
        @Size(max = 2000, message = "Описание модуля не длиннее 2000 символов")
        String description,
        @Min(value = 0, message = "Порядковый номер не может быть отрицательным")
        Integer orderIndex
) {
}
