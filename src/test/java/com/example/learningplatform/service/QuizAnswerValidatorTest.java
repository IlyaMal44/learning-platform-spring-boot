package com.example.learningplatform.service;

import com.example.learningplatform.domain.AnswerOption;
import com.example.learningplatform.domain.Question;
import com.example.learningplatform.dto.request.QuizAnswerRequest;
import com.example.learningplatform.enums.QuestionType;
import com.example.learningplatform.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuizAnswerValidatorTest {

    @Test
    @DisplayName("Полный набор ответов принимается")
    void acceptsCompleteAnswers() {
        Question question = question();
        var selected = QuizAnswerValidator.validate(
                List.of(question),
                List.of(new QuizAnswerRequest(1L, List.of(11L))));
        assertThat(selected).containsEntry(1L, Set.of(11L));
    }

    @Test
    @DisplayName("Пропущенный вопрос отклоняется")
    void rejectsMissingQuestion() {
        Question first = question();
        Question second = question();
        second.setId(2L);
        assertThatThrownBy(() -> QuizAnswerValidator.validate(
                List.of(first, second),
                List.of(new QuizAnswerRequest(1L, List.of(11L)))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Чужой вариант ответа отклоняется")
    void rejectsForeignOption() {
        assertThatThrownBy(() -> QuizAnswerValidator.validate(
                List.of(question()),
                List.of(new QuizAnswerRequest(1L, List.of(99L)))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Несколько вариантов у вопроса с одним ответом отклоняются")
    void rejectsSeveralOptionsForSingleChoice() {
        assertThatThrownBy(() -> QuizAnswerValidator.validate(
                List.of(question()),
                List.of(new QuizAnswerRequest(1L, List.of(10L, 11L)))))
                .isInstanceOf(BusinessRuleException.class);
    }

    private Question question() {
        Question question = new Question();
        question.setId(1L);
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setText("Вопрос");
        question.addOption(option(10L, false));
        question.addOption(option(11L, true));
        return question;
    }

    private AnswerOption option(Long id, boolean correct) {
        AnswerOption option = new AnswerOption();
        option.setId(id);
        option.setText("Вариант " + id);
        option.setCorrect(correct);
        return option;
    }
}
