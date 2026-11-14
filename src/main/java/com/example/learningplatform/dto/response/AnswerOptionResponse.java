package com.example.learningplatform.dto.response;

public record AnswerOptionResponse(
        Long id,
        String text,
        boolean correct
) {
}
