package com.example.learningplatform.controller;

import com.example.learningplatform.dto.request.CreateCourseRequest;
import com.example.learningplatform.dto.request.CreateEnrollmentRequest;
import com.example.learningplatform.dto.request.CreateModuleRequest;
import com.example.learningplatform.dto.request.CreateReviewRequest;
import com.example.learningplatform.dto.request.UpdateCourseRequest;
import com.example.learningplatform.dto.request.UpdateCourseTagsRequest;
import com.example.learningplatform.dto.response.CourseDetailsResponse;
import com.example.learningplatform.dto.response.CourseSummaryResponse;
import com.example.learningplatform.dto.response.EnrollmentResponse;
import com.example.learningplatform.dto.response.ModuleResponse;
import com.example.learningplatform.dto.response.ReviewResponse;
import com.example.learningplatform.dto.response.UserSummaryResponse;
import com.example.learningplatform.service.CourseService;
import com.example.learningplatform.service.EnrollmentService;
import com.example.learningplatform.service.ModuleService;
import com.example.learningplatform.service.ReviewService;
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
@RequestMapping("/api/courses")
@Tag(name = "Курсы")
public class CourseController {

    private final CourseService courseService;
    private final ModuleService moduleService;
    private final EnrollmentService enrollmentService;
    private final ReviewService reviewService;

    public CourseController(
            CourseService courseService,
            ModuleService moduleService,
            EnrollmentService enrollmentService,
            ReviewService reviewService
    ) {
        this.courseService = courseService;
        this.moduleService = moduleService;
        this.enrollmentService = enrollmentService;
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Создать курс")
    public ResponseEntity<CourseDetailsResponse> create(@Valid @RequestBody CreateCourseRequest request) {
        CourseDetailsResponse body = courseService.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + body.id())).body(body);
    }

    @GetMapping
    @Operation(summary = "Список курсов")
    public List<CourseSummaryResponse> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long tagId
    ) {
        return courseService.list(categoryId, teacherId, tagId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Карточка курса со структурой модулей и уроков")
    public CourseDetailsResponse get(@PathVariable Long id) {
        return courseService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить курс")
    public CourseDetailsResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCourseRequest request) {
        return courseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить курс вместе с содержимым")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/tags")
    @Operation(summary = "Заменить теги курса")
    public CourseDetailsResponse replaceTags(@PathVariable Long id, @Valid @RequestBody UpdateCourseTagsRequest request) {
        return courseService.replaceTags(id, request);
    }

    @PostMapping("/{courseId}/modules")
    @Operation(summary = "Добавить модуль в курс")
    public ResponseEntity<ModuleResponse> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request
    ) {
        ModuleResponse body = moduleService.create(courseId, request);
        return ResponseEntity.created(URI.create("/api/modules/" + body.id())).body(body);
    }

    @PostMapping("/{courseId}/enrollments")
    @Operation(summary = "Записать студента на курс")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateEnrollmentRequest request
    ) {
        EnrollmentResponse body = enrollmentService.enroll(courseId, request.studentId());
        return ResponseEntity.created(URI.create("/api/enrollments/" + body.id())).body(body);
    }

    @GetMapping("/{courseId}/enrollments")
    @Operation(summary = "Записи на курс")
    public List<EnrollmentResponse> enrollments(@PathVariable Long courseId) {
        return enrollmentService.listByCourse(courseId);
    }

    @GetMapping("/{courseId}/students")
    @Operation(summary = "Студенты курса")
    public List<UserSummaryResponse> students(@PathVariable Long courseId) {
        return enrollmentService.listStudents(courseId);
    }

    @DeleteMapping("/{courseId}/enrollments/{studentId}")
    @Operation(summary = "Отписать студента от курса")
    public ResponseEntity<Void> unenroll(@PathVariable Long courseId, @PathVariable Long studentId) {
        enrollmentService.unenroll(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/reviews")
    @Operation(summary = "Оставить отзыв о курсе")
    public ResponseEntity<ReviewResponse> review(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewResponse body = reviewService.create(courseId, request);
        return ResponseEntity.created(URI.create("/api/reviews/" + body.id())).body(body);
    }

    @GetMapping("/{courseId}/reviews")
    @Operation(summary = "Отзывы курса")
    public List<ReviewResponse> reviews(@PathVariable Long courseId) {
        return reviewService.listByCourse(courseId);
    }
}
