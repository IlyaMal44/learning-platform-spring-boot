package com.example.learningplatform.service;

import com.example.learningplatform.domain.AnswerOption;
import com.example.learningplatform.domain.Question;
import com.example.learningplatform.enums.QuestionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QuizGraderTest {

    @Test
    @DisplayName("Все ответы с одним вариантом верные — 100 баллов")
    void allSingleChoiceCorrect() {
        Question question = question(1L, QuestionType.SINGLE_CHOICE, option(10L, false), option(11L, true));
        QuizGrader.QuizScore score = QuizGrader.grade(List.of(question), Map.of(1L, Set.of(11L)));
        assertThat(score.score()).isEqualTo(100);
        assertThat(score.correctAnswers()).isEqualTo(1);
        assertThat(score.totalQuestions()).isEqualTo(1);
    }

    @Test
    @DisplayName("Неверный одиночный ответ не даёт балл")
    void singleChoiceWrong() {
        Question question = question(1L, QuestionType.SINGLE_CHOICE, option(10L, false), option(11L, true));
        QuizGrader.QuizScore score = QuizGrader.grade(List.of(question), Map.of(1L, Set.of(10L)));
        assertThat(score.score()).isZero();
        assertThat(score.correctAnswers()).isZero();
    }

    @Test
    @DisplayName("Множественный выбор засчитывается только при точном совпадении")
    void multipleChoiceRequiresExactSet() {
        Question question = question(2L, QuestionType.MULTIPLE_CHOICE,
                option(20L, true), option(21L, true), option(22L, false));
        QuizGrader.QuizScore exact = QuizGrader.grade(List.of(question), Map.of(2L, Set.of(20L, 21L)));
        QuizGrader.QuizScore missing = QuizGrader.grade(List.of(question), Map.of(2L, Set.of(20L)));
        QuizGrader.QuizScore extra = QuizGrader.grade(List.of(question), Map.of(2L, Set.of(20L, 21L, 22L)));
        assertThat(exact.correctAnswers()).isEqualTo(1);
        assertThat(missing.correctAnswers()).isZero();
        assertThat(extra.correctAnswers()).isZero();
    }

    @Test
    @DisplayName("Один верный вопрос из трёх даёт 33 балла")
    void roundsPercentage() {
        List<Question> questions = List.of(
                question(1L, QuestionType.SINGLE_CHOICE, option(11L, true)),
                question(2L, QuestionType.SINGLE_CHOICE, option(21L, true)),
                question(3L, QuestionType.SINGLE_CHOICE, option(31L, true)));
        QuizGrader.QuizScore score = QuizGrader.grade(questions, Map.of(
                1L, Set.of(11L),
                2L, Set.of(99L),
                3L, Set.of()));
        assertThat(score.correctAnswers()).isEqualTo(1);
        assertThat(score.score()).isEqualTo(33);
    }

    @Test
    @DisplayName("Пустой тест даёт ноль")
    void emptyQuiz() {
        QuizGrader.QuizScore score = QuizGrader.grade(List.of(), Map.of());
        assertThat(score.score()).isZero();
        assertThat(score.totalQuestions()).isZero();
    }

    private Question question(Long id, QuestionType type, AnswerOption... options) {
        Question question = new Question();
        question.setId(id);
        question.setType(type);
        question.setText("Вопрос " + id);
        for (AnswerOption option : options) {
            question.addOption(option);
        }
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
