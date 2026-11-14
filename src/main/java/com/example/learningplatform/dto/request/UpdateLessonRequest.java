package com.example.learningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLessonRequest(
        @NotBlank(message = "Название урока обязательно")
        @Size(max = 200, message = "Название урока не длиннее 200 символов")
        String title,
        @Size(max = 10000, message = "Текст урока не длиннее 10000 символов")
        String content,
        @Size(max = 500, message = "Ссылка на видео не длиннее 500 символов")
        String videoUrl
) {
}
