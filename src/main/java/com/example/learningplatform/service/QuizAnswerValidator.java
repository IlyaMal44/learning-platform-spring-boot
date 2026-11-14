package com.example.learningplatform.service;

import com.example.learningplatform.domain.Question;
import com.example.learningplatform.dto.request.QuizAnswerRequest;
import com.example.learningplatform.enums.QuestionType;
import com.example.learningplatform.exception.BusinessRuleException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class QuizAnswerValidator {

    private QuizAnswerValidator() {
    }

    public static Map<Long, Set<Long>> validate(List<Question> questions, List<QuizAnswerRequest> answers) {
        Map<Long, Question> questionsById = questions.stream()
                .collect(Collectors.toMap(Question::getId, question -> question));
        Map<Long, Set<Long>> selected = new HashMap<>();
        for (QuizAnswerRequest answer : answers) {
            if (selected.containsKey(answer.questionId())) {
                throw new BusinessRuleException("Вопрос " + answer.questionId() + " указан повторно");
            }
            Question question = questionsById.get(answer.questionId());
            if (question == null) {
                throw new BusinessRuleException("Вопрос " + answer.questionId() + " не входит в этот тест");
            }
            Set<Long> optionIds = new HashSet<>();
            for (Long optionId : answer.optionIds()) {
                if (!optionIds.add(optionId)) {
                    throw new BusinessRuleException("Вариант " + optionId + " указан повторно");
                }
                boolean belongs = question.getOptions().stream().anyMatch(option -> option.getId().equals(optionId));
                if (!belongs) {
                    throw new BusinessRuleException("Вариант " + optionId + " не относится к вопросу " + question.getId());
                }
            }
            if (question.getType() == QuestionType.SINGLE_CHOICE && optionIds.size() != 1) {
                throw new BusinessRuleException("Для вопроса с одним ответом нужно выбрать ровно один вариант");
            }
            selected.put(question.getId(), optionIds);
        }
        if (!selected.keySet().equals(questionsById.keySet())) {
            throw new BusinessRuleException("Нужно ответить на все вопросы теста");
        }
        return selected;
    }
}
