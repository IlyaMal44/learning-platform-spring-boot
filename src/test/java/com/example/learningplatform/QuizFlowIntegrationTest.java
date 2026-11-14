package com.example.learningplatform;

import com.example.learningplatform.dto.request.CreateAnswerOptionRequest;
import com.example.learningplatform.dto.request.CreateQuestionRequest;
import com.example.learningplatform.dto.request.CreateQuizRequest;
import com.example.learningplatform.dto.request.QuizAnswerRequest;
import com.example.learningplatform.dto.request.TakeQuizRequest;
import com.example.learningplatform.dto.response.QuizResponse;
import com.example.learningplatform.enums.QuestionType;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.support.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuizFlowIntegrationTest extends ServiceTestSupport {

    @Test
    @DisplayName("У модуля один тест, прохождение сохраняет процент верных ответов")
    void takesQuizAndStoresScore() {
        var teacher = teacher();
        var student = student();
        var course = courseWithAssignment(teacher, category());
        enrollmentService.enroll(course.id(), student.id());
        Long moduleId = course.modules().getFirst().id();

        QuizResponse quiz = quizService.create(moduleId, new CreateQuizRequest("Проверка", 10));
        quiz = quizService.addQuestion(quiz.id(), new CreateQuestionRequest(
                "Ленивая коллекция?",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new CreateAnswerOptionRequest("Да", true),
                        new CreateAnswerOptionRequest("Нет", false))));
        quiz = quizService.addQuestion(quiz.id(), new CreateQuestionRequest(
                "Что есть у курса?",
                QuestionType.MULTIPLE_CHOICE,
                List.of(
                        new CreateAnswerOptionRequest("Модули", true),
                        new CreateAnswerOptionRequest("Преподаватель", true),
                        new CreateAnswerOptionRequest("Пароль", false))));

        assertThatThrownBy(() -> quizService.create(moduleId, new CreateQuizRequest("Второй", 5)))
                .isInstanceOf(ConflictException.class);

        var correct = quiz.questions().getFirst().options().getFirst();
        var wrongMultiple = quiz.questions().get(1).options().get(2);
        var attempt = quizService.take(quiz.id(), new TakeQuizRequest(student.id(), List.of(
                new QuizAnswerRequest(quiz.questions().getFirst().id(), List.of(correct.id())),
                new QuizAnswerRequest(quiz.questions().get(1).id(), List.of(wrongMultiple.id())))));

        assertThat(attempt.correctAnswers()).isEqualTo(1);
        assertThat(attempt.totalQuestions()).isEqualTo(2);
        assertThat(attempt.score()).isEqualTo(50);
        assertThat(quizService.listResults(quiz.id())).extracting(result -> result.score()).containsExactly(50);
        assertThat(quizService.listResultsByStudent(student.id())).hasSize(1);
        assertThat(courseService.get(course.id()).modules().getFirst().quiz().id()).isEqualTo(quiz.id());
    }

    @Test
    @DisplayName("Незаписанный студент не может пройти тест")
    void outsiderCannotTakeQuiz() {
        var course = courseWithAssignment(teacher(), category());
        var outsider = student();
        QuizResponse quiz = quizService.create(course.modules().getFirst().id(), new CreateQuizRequest("Тест", null));
        quiz = quizService.addQuestion(quiz.id(), new CreateQuestionRequest(
                "Вопрос",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new CreateAnswerOptionRequest("А", true),
                        new CreateAnswerOptionRequest("Б", false))));
        Long quizId = quiz.id();
        Long questionId = quiz.questions().getFirst().id();
        Long optionId = quiz.questions().getFirst().options().getFirst().id();

        assertThatThrownBy(() -> quizService.take(quizId, new TakeQuizRequest(
                outsider.id(),
                List.of(new QuizAnswerRequest(questionId, List.of(optionId))))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Вопрос с одним ответом не может иметь два правильных варианта")
    void rejectsInvalidQuestion() {
        var course = courseWithAssignment(teacher(), category());
        QuizResponse quiz = quizService.create(course.modules().getFirst().id(), new CreateQuizRequest("Тест", 5));
        assertThatThrownBy(() -> quizService.addQuestion(quiz.id(), new CreateQuestionRequest(
                "Два верных",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new CreateAnswerOptionRequest("А", true),
                        new CreateAnswerOptionRequest("Б", true)))))
                .isInstanceOf(BusinessRuleException.class);
    }
}
