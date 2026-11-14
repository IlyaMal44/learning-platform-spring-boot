package com.example.learningplatform;

import com.example.learningplatform.dto.request.CreateCourseRequest;
import com.example.learningplatform.dto.request.CreateSubmissionRequest;
import com.example.learningplatform.dto.request.UpdateCourseRequest;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.support.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseContentIntegrationTest extends ServiceTestSupport {

    @Test
    @DisplayName("Курс создаётся с категорией и преподавателем, структура читается целиком")
    void createsCourseStructure() {
        var teacher = teacher();
        var category = category();
        var details = courseWithAssignment(teacher, category);

        assertThat(details.category().id()).isEqualTo(category.id());
        assertThat(details.teacher().id()).isEqualTo(teacher.id());
        assertThat(details.modules()).hasSize(1);
        assertThat(details.modules().getFirst().lessons()).hasSize(1);
        assertThat(details.modules().getFirst().lessons().getFirst().assignments()).hasSize(1);

        var listed = courseService.list(category.id(), teacher.id(), null);
        assertThat(listed).extracting(course -> course.id()).contains(details.id());
    }

    @Test
    @DisplayName("Студент не может быть преподавателем курса")
    void studentCannotTeach() {
        var student = student();
        var category = category();
        assertThatThrownBy(() -> courseService.create(new CreateCourseRequest(
                "Недоступный курс",
                "Описание",
                category.id(),
                student.id(),
                null,
                null,
                List.of())))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Описание курса обновляется")
    void updatesCourse() {
        var teacher = teacher();
        var created = course(teacher, category());
        var updated = courseService.update(created.id(), new UpdateCourseRequest(
                "Новое название",
                "Новое описание",
                created.category().id(),
                teacher.id(),
                "8 недель",
                LocalDate.of(2026, 12, 1)));

        assertThat(updated.title()).isEqualTo("Новое название");
        assertThat(updated.description()).isEqualTo("Новое описание");
        assertThat(courseRepository.findById(created.id()).orElseThrow().getDuration()).isEqualTo("8 недель");
    }

    @Test
    @DisplayName("Удаление курса каскадно удаляет модули, уроки, задания и решения")
    void deletingCourseRemovesContent() {
        var teacher = teacher();
        var student = student();
        var details = courseWithAssignment(teacher, category());
        enrollmentService.enroll(details.id(), student.id());
        var assignment = details.modules().getFirst().lessons().getFirst().assignments().getFirst();
        var submission = submissionService.submit(assignment.id(), new CreateSubmissionRequest(student.id(), "Ответ"));
        Long moduleId = details.modules().getFirst().id();
        Long lessonId = details.modules().getFirst().lessons().getFirst().id();

        courseService.delete(details.id());

        assertThat(courseRepository.findById(details.id())).isEmpty();
        assertThat(moduleRepository.findById(moduleId)).isEmpty();
        assertThat(lessonRepository.findById(lessonId)).isEmpty();
        assertThat(assignmentRepository.findById(assignment.id())).isEmpty();
        assertThat(submissionRepository.findById(submission.id())).isEmpty();
        assertThat(userRepository.findById(student.id())).isPresent();
        assertThat(userRepository.findById(teacher.id())).isPresent();
    }

    @Test
    @DisplayName("Чтение несуществующего курса завершается ошибкой")
    void missingCourse() {
        assertThatThrownBy(() -> courseService.get(9_999_999L))
                .isInstanceOf(com.example.learningplatform.exception.NotFoundException.class);
    }

    @Test
    @DisplayName("Преподавателя можно заменить на другого преподавателя")
    void replacesTeacher() {
        var first = teacher();
        var second = teacher();
        var created = course(first, category());
        var updated = courseService.update(created.id(), new UpdateCourseRequest(
                created.title(),
                created.description(),
                created.category().id(),
                second.id(),
                created.duration(),
                created.startDate()));
        assertThat(updated.teacher().id()).isEqualTo(second.id());
        assertThat(updated.teacher().role()).isEqualTo(Role.TEACHER);
    }
}
