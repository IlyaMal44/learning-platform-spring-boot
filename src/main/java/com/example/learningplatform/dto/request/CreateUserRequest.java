package com.example.learningplatform.dto.request;

import com.example.learningplatform.enums.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Имя обязательно")
        @Size(max = 150, message = "Имя не длиннее 150 символов")
        String name,
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный email")
        @Size(max = 255, message = "Email не длиннее 255 символов")
        String email,
        @NotNull(message = "Роль обязательна")
        Role role,
        @Valid
        UpsertProfileRequest profile
) {
}
