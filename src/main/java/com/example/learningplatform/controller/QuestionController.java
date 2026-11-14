package com.example.learningplatform.controller;

import com.example.learningplatform.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Вопросы")
public class QuestionController {

    private final QuizService quizService;

    public QuestionController(QuizService quizService) {
        this.quizService = quizService;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить вопрос")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
