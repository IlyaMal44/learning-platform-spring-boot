package com.example.learningplatform.dto.response;

import com.example.learningplatform.enums.QuestionType;

import java.util.List;

public record QuestionResponse(
        Long id,
        String text,
        QuestionType type,
        List<AnswerOptionResponse> options
) {
}
