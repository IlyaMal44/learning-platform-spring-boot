package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.CreateUserRequest;
import com.example.learningplatform.dto.request.UpdateUserRequest;
import com.example.learningplatform.dto.request.UpsertProfileRequest;
import com.example.learningplatform.dto.response.EnrollmentResponse;
import com.example.learningplatform.dto.response.ProfileResponse;
import com.example.learningplatform.dto.response.QuizResultResponse;
import com.example.learningplatform.dto.response.ReviewResponse;
import com.example.learningplatform.dto.response.SubmissionResponse;
import com.example.learningplatform.dto.response.UserResponse;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.service.EnrollmentService;
import com.example.learningplatform.service.QuizService;
import com.example.learningplatform.service.ReviewService;
import com.example.learningplatform.service.SubmissionService;
import com.example.learningplatform.service.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи")
public class UserController {

    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;
    private final ReviewService reviewService;

    public UserController(
            UserService userService,
            EnrollmentService enrollmentService,
            SubmissionService submissionService,
            QuizService quizService,
            ReviewService reviewService
    ) {
        this.userService = userService;
        this.enrollmentService = enrollmentService;
        this.submissionService = submissionService;
        this.quizService = quizService;
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse body = userService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + body.id())).body(body);
    }

    @GetMapping
    @Operation(summary = "Список пользователей")
    public List<UserResponse> list(@RequestParam(required = false) Role role) {
        return userService.list(role);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя")
    public UserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "Создать или обновить профиль")
    public ProfileResponse upsertProfile(@PathVariable Long id, @Valid @RequestBody UpsertProfileRequest request) {
        return userService.upsertProfile(id, request);
    }

    @GetMapping("/{id}/courses")
    @Operation(summary = "Курсы, на которые записан студент")
    public List<EnrollmentResponse> courses(@PathVariable Long id) {
        return enrollmentService.listByStudent(id);
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "Решения студента")
    public List<SubmissionResponse> submissions(@PathVariable Long id) {
        return submissionService.listByStudent(id);
    }

    @GetMapping("/{id}/quiz-results")
    @Operation(summary = "Результаты тестов студента")
    public List<QuizResultResponse> quizResults(@PathVariable Long id) {
        return quizService.listResultsByStudent(id);
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Отзывы студента")
    public List<ReviewResponse> reviews(@PathVariable Long id) {
        return reviewService.listByStudent(id);
    }
}
