package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.CreateQuestionRequest;
import com.example.learningplatform.dto.request.TakeQuizRequest;
import com.example.learningplatform.dto.request.UpdateQuizRequest;
import com.example.learningplatform.dto.response.QuizAttemptResponse;
import com.example.learningplatform.dto.response.QuizResponse;
import com.example.learningplatform.dto.response.QuizResultResponse;
import com.example.learningplatform.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@Tag(name = "Тесты")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить тест с вопросами")
    public QuizResponse get(@PathVariable Long id) {
        return quizService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить тест")
    public QuizResponse update(@PathVariable Long id, @Valid @RequestBody UpdateQuizRequest request) {
        return quizService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тест")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/questions")
    @Operation(summary = "Добавить вопрос")
    public QuizResponse addQuestion(@PathVariable Long id, @Valid @RequestBody CreateQuestionRequest request) {
        return quizService.addQuestion(id, request);
    }

    @PostMapping("/{id}/attempts")
    @Operation(summary = "Пройти тест")
    public ResponseEntity<QuizAttemptResponse> take(@PathVariable Long id, @Valid @RequestBody TakeQuizRequest request) {
        QuizAttemptResponse body = quizService.take(id, request);
        return ResponseEntity.created(URI.create("/api/quizzes/" + id + "/results")).body(body);
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Результаты теста")
    public List<QuizResultResponse> results(@PathVariable Long id) {
        return quizService.listResults(id);
    }
}
