package com.example.learningplatform;

import com.example.learningplatform.dto.request.CreateCategoryRequest;
import com.example.learningplatform.dto.request.CreateCourseRequest;
import com.example.learningplatform.dto.request.CreateEnrollmentRequest;
import com.example.learningplatform.dto.request.CreateModuleRequest;
import com.example.learningplatform.dto.request.CreateUserRequest;
import com.example.learningplatform.dto.response.CategoryResponse;
import com.example.learningplatform.dto.response.CourseDetailsResponse;
import com.example.learningplatform.dto.response.UserResponse;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestRestTemplate
class LearningPlatformApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Невалидное тело запроса возвращает 400 и список полей")
    void validationErrorReturns400() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(new CreateUserRequest(" ", "not-an-email", null, null)),
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errors")).isInstanceOf(Map.class);
    }

    @Test
    @DisplayName("Несуществующий курс возвращает 404")
    void missingCourseReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/courses/99999999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("API создаёт курс, добавляет модуль и не даёт записаться дважды")
    void courseFlowThroughHttp() {
        CategoryResponse category = restTemplate.postForEntity(
                "/api/categories",
                new CreateCategoryRequest("API " + UUID.randomUUID()),
                CategoryResponse.class).getBody();
        UserResponse teacher = restTemplate.postForEntity(
                "/api/users",
                new CreateUserRequest("Преподаватель API", "teacher-" + UUID.randomUUID() + "@example.com", Role.TEACHER, null),
                UserResponse.class).getBody();
        UserResponse student = restTemplate.postForEntity(
                "/api/users",
                new CreateUserRequest("Студент API", "student-" + UUID.randomUUID() + "@example.com", Role.STUDENT, null),
                UserResponse.class).getBody();
        assertThat(category).isNotNull();
        assertThat(teacher).isNotNull();
        assertThat(student).isNotNull();

        ResponseEntity<CourseDetailsResponse> created = restTemplate.postForEntity(
                "/api/courses",
                new CreateCourseRequest(
                        "API курс",
                        "Описание",
                        category.id(),
                        teacher.id(),
                        "2 недели",
                        LocalDate.of(2026, 11, 14),
                        List.of()),
                CourseDetailsResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();

        ResponseEntity<String> module = restTemplate.postForEntity(
                "/api/courses/" + created.getBody().id() + "/modules",
                new CreateModuleRequest("Модуль API", "Описание", 0),
                String.class);
        assertThat(module.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CourseDetailsResponse loaded = restTemplate.getForObject(
                "/api/courses/" + created.getBody().id(),
                CourseDetailsResponse.class);
        assertThat(loaded).isNotNull();
        assertThat(loaded.modules()).hasSize(1);
        assertThat(loaded.teacher().name()).isEqualTo("Преподаватель API");

        ResponseEntity<String> enrollment = restTemplate.postForEntity(
                "/api/courses/" + created.getBody().id() + "/enrollments",
                new CreateEnrollmentRequest(student.id()),
                String.class);
        assertThat(enrollment.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> duplicate = restTemplate.postForEntity(
                "/api/courses/" + created.getBody().id() + "/enrollments",
                new CreateEnrollmentRequest(student.id()),
                String.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                "/api/courses/" + created.getBody().id(),
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
