package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.CreateAssignmentRequest;
import com.example.learningplatform.dto.request.UpdateLessonRequest;
import com.example.learningplatform.dto.response.AssignmentResponse;
import com.example.learningplatform.dto.response.LessonResponse;
import com.example.learningplatform.service.AssignmentService;
import com.example.learningplatform.service.LessonService;
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
@RequestMapping("/api/lessons")
@Tag(name = "Уроки")
public class LessonController {

    private final LessonService lessonService;
    private final AssignmentService assignmentService;

    public LessonController(LessonService lessonService, AssignmentService assignmentService) {
        this.lessonService = lessonService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить урок")
    public LessonResponse get(@PathVariable Long id) {
        return lessonService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить урок")
    public LessonResponse update(@PathVariable Long id, @Valid @RequestBody UpdateLessonRequest request) {
        return lessonService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить урок")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lessonId}/assignments")
    @Operation(summary = "Добавить задание к уроку")
    public ResponseEntity<AssignmentResponse> addAssignment(
            @PathVariable Long lessonId,
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        AssignmentResponse body = assignmentService.create(lessonId, request);
        return ResponseEntity.created(URI.create("/api/assignments/" + body.id())).body(body);
    }
}
