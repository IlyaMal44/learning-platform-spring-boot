package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.Size;

public record UpsertProfileRequest(
        @Size(max = 2000, message = "Биография не длиннее 2000 символов")
        String bio,
        @Size(max = 500, message = "Ссылка на аватар не длиннее 500 символов")
        String avatarUrl
) {
}
