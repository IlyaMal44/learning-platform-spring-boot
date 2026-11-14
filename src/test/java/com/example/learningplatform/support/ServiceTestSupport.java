package com.example.learningplatform.support;

import com.example.learningplatform.dto.request.CreateAssignmentRequest;
import com.example.learningplatform.dto.request.CreateCategoryRequest;
import com.example.learningplatform.dto.request.CreateCourseRequest;
import com.example.learningplatform.dto.request.CreateLessonRequest;
import com.example.learningplatform.dto.request.CreateModuleRequest;
import com.example.learningplatform.dto.request.CreateUserRequest;
import com.example.learningplatform.dto.request.UpsertProfileRequest;
import com.example.learningplatform.dto.response.CategoryResponse;
import com.example.learningplatform.dto.response.CourseDetailsResponse;
import com.example.learningplatform.dto.response.UserResponse;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.repository.AssignmentRepository;
import com.example.learningplatform.repository.CourseRepository;
import com.example.learningplatform.repository.EnrollmentRepository;
import com.example.learningplatform.repository.LessonRepository;
import com.example.learningplatform.repository.ModuleRepository;
import com.example.learningplatform.repository.ProfileRepository;
import com.example.learningplatform.repository.QuizRepository;
import com.example.learningplatform.repository.SubmissionRepository;
import com.example.learningplatform.repository.UserRepository;
import com.example.learningplatform.service.AssignmentService;
import com.example.learningplatform.service.CategoryService;
import com.example.learningplatform.service.CourseService;
import com.example.learningplatform.service.EnrollmentService;
import com.example.learningplatform.service.LessonService;
import com.example.learningplatform.service.ModuleService;
import com.example.learningplatform.service.QuizService;
import com.example.learningplatform.service.ReviewService;
import com.example.learningplatform.service.SubmissionService;
import com.example.learningplatform.service.TagService;
import com.example.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public abstract class ServiceTestSupport extends AbstractIntegrationTest {

    @Autowired
    protected UserService userService;
    @Autowired
    protected CategoryService categoryService;
    @Autowired
    protected TagService tagService;
    @Autowired
    protected CourseService courseService;
    @Autowired
    protected ModuleService moduleService;
    @Autowired
    protected LessonService lessonService;
    @Autowired
    protected AssignmentService assignmentService;
    @Autowired
    protected EnrollmentService enrollmentService;
    @Autowired
    protected SubmissionService submissionService;
    @Autowired
    protected QuizService quizService;
    @Autowired
    protected ReviewService reviewService;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected ProfileRepository profileRepository;
    @Autowired
    protected CourseRepository courseRepository;
    @Autowired
    protected ModuleRepository moduleRepository;
    @Autowired
    protected LessonRepository lessonRepository;
    @Autowired
    protected AssignmentRepository assignmentRepository;
    @Autowired
    protected SubmissionRepository submissionRepository;
    @Autowired
    protected EnrollmentRepository enrollmentRepository;
    @Autowired
    protected QuizRepository quizRepository;

    protected UserResponse student() {
        return user(Role.STUDENT, "Студент");
    }

    protected UserResponse teacher() {
        return user(Role.TEACHER, "Преподаватель");
    }

    protected UserResponse admin() {
        return user(Role.ADMIN, "Администратор");
    }

    protected UserResponse user(Role role, String name) {
        return userService.create(new CreateUserRequest(
                name,
                role.name().toLowerCase() + "-" + UUID.randomUUID() + "@example.com",
                role,
                new UpsertProfileRequest("Профиль " + name, null)));
    }

    protected CategoryResponse category() {
        return categoryService.create(new CreateCategoryRequest("Категория " + UUID.randomUUID()));
    }

    protected CourseDetailsResponse course(UserResponse teacher, CategoryResponse category) {
        return courseService.create(new CreateCourseRequest(
                "Курс " + UUID.randomUUID(),
                "Описание курса",
                category.id(),
                teacher.id(),
                "4 недели",
                LocalDate.of(2026, 11, 1),
                List.of()));
    }

    protected CourseDetailsResponse courseWithAssignment(UserResponse teacher, CategoryResponse category) {
        CourseDetailsResponse created = course(teacher, category);
        var module = moduleService.create(created.id(), new CreateModuleRequest("Модуль", "Раздел", 0));
        var lesson = lessonService.create(module.id(), new CreateLessonRequest("Урок", "Материал урока", null));
        assignmentService.create(lesson.id(), new CreateAssignmentRequest(
                "Задание",
                "Сделайте работу",
                LocalDate.now().plusDays(3),
                100));
        return courseService.get(created.id());
    }
}
