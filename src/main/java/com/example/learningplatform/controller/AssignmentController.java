package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.CreateSubmissionRequest;
import com.example.learningplatform.dto.request.UpdateAssignmentRequest;
import com.example.learningplatform.dto.response.AssignmentResponse;
import com.example.learningplatform.dto.response.SubmissionResponse;
import com.example.learningplatform.service.AssignmentService;
import com.example.learningplatform.service.SubmissionService;
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
@RequestMapping("/api/assignments")
@Tag(name = "Задания")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;

    public AssignmentController(AssignmentService assignmentService, SubmissionService submissionService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить задание")
    public AssignmentResponse get(@PathVariable Long id) {
        return assignmentService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить задание")
    public AssignmentResponse update(@PathVariable Long id, @Valid @RequestBody UpdateAssignmentRequest request) {
        return assignmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить задание")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "Решения по заданию")
    public List<SubmissionResponse> submissions(@PathVariable Long id) {
        return submissionService.listByAssignment(id);
    }

    @PostMapping("/{id}/submissions")
    @Operation(summary = "Отправить решение")
    public ResponseEntity<SubmissionResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody CreateSubmissionRequest request
    ) {
        SubmissionResponse body = submissionService.submit(id, request);
        return ResponseEntity.created(URI.create("/api/submissions/" + body.id())).body(body);
    }
}
