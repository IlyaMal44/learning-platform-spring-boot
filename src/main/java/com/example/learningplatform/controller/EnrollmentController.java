package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.UpdateEnrollmentRequest;
import com.example.learningplatform.dto.response.EnrollmentResponse;
import com.example.learningplatform.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Записи на курс")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Изменить статус записи")
    public EnrollmentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateEnrollmentRequest request) {
        return enrollmentService.updateStatus(id, request);
    }
}
