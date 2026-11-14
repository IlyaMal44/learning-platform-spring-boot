package com.example.learningplatform;

import com.example.learningplatform.domain.Category;
import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Lesson;
import com.example.learningplatform.domain.Module;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.request.CreateTagRequest;
import com.example.learningplatform.dto.request.CreateUserRequest;
import com.example.learningplatform.dto.request.UpdateCourseTagsRequest;
import com.example.learningplatform.dto.request.UpdateUserRequest;
import com.example.learningplatform.dto.request.UpsertProfileRequest;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.repository.CategoryRepository;
import com.example.learningplatform.support.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAndCatalogIntegrationTest extends ServiceTestSupport {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Пользователь сохраняется вместе с профилем один к одному")
    void createsUserWithProfile() {
        var created = userService.create(new CreateUserRequest(
                "Ольга",
                "olga-" + UUID.randomUUID() + "@example.com",
                Role.STUDENT,
                new UpsertProfileRequest("Люблю SQL", "https://example.com/olga.png")));

        var loaded = userService.get(created.id());
        assertThat(loaded.profile()).isNotNull();
        assertThat(loaded.profile().bio()).isEqualTo("Люблю SQL");
        assertThat(loaded.profile().avatarUrl()).isEqualTo("https://example.com/olga.png");
        assertThat(profileRepository.findById(loaded.profile().id())).isPresent();
    }

    @Test
    @DisplayName("Повторный email отклоняется")
    void rejectsDuplicateEmail() {
        String email = "same-" + UUID.randomUUID() + "@example.com";
        userService.create(new CreateUserRequest("Первый", email, Role.STUDENT, null));
        assertThatThrownBy(() -> userService.create(new CreateUserRequest("Второй", email.toUpperCase(), Role.TEACHER, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Удаление пользователя удаляет профиль и не удаляет преподавателя курса")
    void deletesStudentAndKeepsTeacher() {
        var student = student();
        var teacher = teacher();
        var category = category();
        course(teacher, category);
        Long profileId = student.profile().id();

        userService.delete(student.id());

        assertThat(userRepository.findById(student.id())).isEmpty();
        assertThat(profileRepository.findById(profileId)).isEmpty();
        assertThatThrownBy(() -> userService.delete(teacher.id())).isInstanceOf(ConflictException.class);
        assertThat(userRepository.findById(teacher.id())).isPresent();
    }

    @Test
    @DisplayName("Роль преподавателя с курсами нельзя сменить на студента")
    void cannotDemoteTeacher() {
        var teacher = teacher();
        course(teacher, category());
        assertThatThrownBy(() -> userService.update(teacher.id(), new UpdateUserRequest(
                teacher.name(), teacher.email(), Role.STUDENT)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Категорию с курсами удалить нельзя, пустую можно")
    void categoryDeleteDependsOnCourses() {
        var used = category();
        var free = category();
        course(teacher(), used);

        assertThatThrownBy(() -> categoryService.delete(used.id())).isInstanceOf(ConflictException.class);
        categoryService.delete(free.id());
        assertThat(categoryService.list()).extracting(category -> category.id()).doesNotContain(free.id());
    }

    @Test
    @DisplayName("Теги связывают курс многие ко многим и удаляются без курса")
    void tagsAreManyToMany() {
        var java = tagService.create(new CreateTagRequest("Java " + UUID.randomUUID()));
        var sql = tagService.create(new CreateTagRequest("SQL " + UUID.randomUUID()));
        var created = course(teacher(), category());
        var updated = courseService.replaceTags(created.id(), new UpdateCourseTagsRequest(List.of(java.id(), sql.id())));

        assertThat(updated.tags()).extracting(tag -> tag.id()).containsExactlyInAnyOrder(java.id(), sql.id());
        tagService.delete(sql.id());
        assertThat(courseService.get(created.id()).tags()).extracting(tag -> tag.id()).containsExactly(java.id());
    }

    @Test
    @Transactional
    @DisplayName("Сохранение курса каскадно сохраняет новый модуль и урок")
    void cascadePersistsModuleAndLesson() {
        User teacher = userRepository.findById(teacher().id()).orElseThrow();
        Category category = categoryRepository.findById(category().id()).orElseThrow();
        Course course = new Course();
        course.setTitle("Каскад");
        course.setTeacher(teacher);
        course.setCategory(category);
        Module module = new Module();
        module.setTitle("Каскадный модуль");
        module.setOrderIndex(3);
        Lesson lesson = new Lesson();
        lesson.setTitle("Каскадный урок");
        module.addLesson(lesson);
        course.addModule(module);

        courseRepository.saveAndFlush(course);

        assertThat(module.getId()).isNotNull();
        assertThat(lesson.getId()).isNotNull();
        assertThat(moduleRepository.findById(module.getId())).isPresent();
        assertThat(lessonRepository.findById(lesson.getId())).isPresent();
    }
}
