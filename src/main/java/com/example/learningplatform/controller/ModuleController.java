package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.CreateLessonRequest;
import com.example.learningplatform.dto.request.CreateQuizRequest;
import com.example.learningplatform.dto.request.UpdateModuleRequest;
import com.example.learningplatform.dto.response.LessonResponse;
import com.example.learningplatform.dto.response.ModuleResponse;
import com.example.learningplatform.dto.response.QuizResponse;
import com.example.learningplatform.service.LessonService;
import com.example.learningplatform.service.ModuleService;
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

@RestController
@RequestMapping("/api/modules")
@Tag(name = "Модули")
public class ModuleController {

    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final QuizService quizService;

    public ModuleController(ModuleService moduleService, LessonService lessonService, QuizService quizService) {
        this.moduleService = moduleService;
        this.lessonService = lessonService;
        this.quizService = quizService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить модуль")
    public ModuleResponse get(@PathVariable Long id) {
        return moduleService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить модуль")
    public ModuleResponse update(@PathVariable Long id, @Valid @RequestBody UpdateModuleRequest request) {
        return moduleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить модуль")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{moduleId}/lessons")
    @Operation(summary = "Добавить урок в модуль")
    public ResponseEntity<LessonResponse> addLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateLessonRequest request
    ) {
        LessonResponse body = lessonService.create(moduleId, request);
        return ResponseEntity.created(URI.create("/api/lessons/" + body.id())).body(body);
    }

    @PostMapping("/{moduleId}/quiz")
    @Operation(summary = "Создать тест модуля")
    public ResponseEntity<QuizResponse> addQuiz(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        QuizResponse body = quizService.create(moduleId, request);
        return ResponseEntity.created(URI.create("/api/quizzes/" + body.id())).body(body);
    }
}
