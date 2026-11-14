package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.GradeSubmissionRequest;
import com.example.learningplatform.dto.response.SubmissionResponse;
import com.example.learningplatform.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
@Tag(name = "Решения")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить решение")
    public SubmissionResponse get(@PathVariable Long id) {
        return submissionService.get(id);
    }

    @PatchMapping("/{id}/grade")
    @Operation(summary = "Поставить оценку")
    public SubmissionResponse grade(@PathVariable Long id, @Valid @RequestBody GradeSubmissionRequest request) {
        return submissionService.grade(id, request);
    }
}
