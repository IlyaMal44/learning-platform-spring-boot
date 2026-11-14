package com.example.learningplatform.service;

import com.example.learningplatform.domain.AnswerOption;
import com.example.learningplatform.domain.Question;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class QuizGrader {

    private QuizGrader() {
    }

    public static QuizScore grade(List<Question> questions, Map<Long, Set<Long>> selectedByQuestion) {
        int correct = 0;
        for (Question question : questions) {
            Set<Long> selected = selectedByQuestion.getOrDefault(question.getId(), Set.of());
            Set<Long> expected = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());
            if (!expected.isEmpty() && expected.equals(selected)) {
                correct++;
            }
        }
        int total = questions.size();
        int score = total == 0 ? 0 : (int) Math.round(correct * 100.0 / total);
        return new QuizScore(score, correct, total);
    }

    public record QuizScore(int score, int correctAnswers, int totalQuestions) {
    }
}
