package com.example.learningplatform.mapper;

import com.example.learningplatform.domain.AnswerOption;
import com.example.learningplatform.domain.Question;
import com.example.learningplatform.domain.Quiz;
import com.example.learningplatform.domain.QuizSubmission;
import com.example.learningplatform.dto.response.AnswerOptionResponse;
import com.example.learningplatform.dto.response.QuestionResponse;
import com.example.learningplatform.dto.response.QuizResponse;
import com.example.learningplatform.dto.response.QuizResultResponse;

public final class QuizMapper {

    private QuizMapper() {
    }

    public static QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getModule().getId(),
                quiz.getTitle(),
                quiz.getTimeLimit(),
                quiz.getQuestions().stream().map(QuizMapper::toQuestion).toList());
    }

    public static QuestionResponse toQuestion(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getOptions().stream().map(QuizMapper::toOption).toList());
    }

    public static AnswerOptionResponse toOption(AnswerOption option) {
        return new AnswerOptionResponse(option.getId(), option.getText(), option.isCorrect());
    }

    public static QuizResultResponse toResult(QuizSubmission submission) {
        return new QuizResultResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getQuiz().getTitle(),
                submission.getStudent().getId(),
                submission.getStudent().getName(),
                submission.getScore(),
                submission.getTakenAt());
    }
}
