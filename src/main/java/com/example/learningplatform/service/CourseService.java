package com.example.learningplatform.service;

import com.example.learningplatform.domain.Category;
import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Tag;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.request.CreateCourseRequest;
import com.example.learningplatform.dto.request.UpdateCourseRequest;
import com.example.learningplatform.dto.request.UpdateCourseTagsRequest;
import com.example.learningplatform.dto.response.CourseDetailsResponse;
import com.example.learningplatform.dto.response.CourseSummaryResponse;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.mapper.CourseMapper;
import com.example.learningplatform.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final TagService tagService;

    public CourseService(
            CourseRepository courseRepository,
            CategoryService categoryService,
            UserService userService,
            TagService tagService
    ) {
        this.courseRepository = courseRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.tagService = tagService;
    }

    @Transactional
    public CourseDetailsResponse create(CreateCourseRequest request) {
        Course course = new Course();
        apply(course, request.title(), request.description(), request.categoryId(), request.teacherId(),
                request.duration(), request.startDate());
        replaceTags(course, request.tagIds() == null ? List.of() : request.tagIds());
        Course saved = courseRepository.save(course);
        log.info("Создан курс id={} title={}", saved.getId(), saved.getTitle());
        return CourseMapper.toDetails(saved);
    }

    @Transactional
    public CourseDetailsResponse update(Long id, UpdateCourseRequest request) {
        Course course = requireCourse(id);
        apply(course, request.title(), request.description(), request.categoryId(), request.teacherId(),
                request.duration(), request.startDate());
        log.info("Обновлён курс id={}", id);
        return CourseMapper.toDetails(course);
    }

    public CourseDetailsResponse get(Long id) {
        return CourseMapper.toDetails(requireCourse(id));
    }

    public List<CourseSummaryResponse> list(Long categoryId, Long teacherId, Long tagId) {
        if (categoryId != null) {
            categoryService.requireCategory(categoryId);
        }
        if (teacherId != null) {
            userService.requireUser(teacherId);
        }
        if (tagId != null) {
            tagService.requireTag(tagId);
        }
        return courseRepository.findAll().stream()
                .filter(course -> categoryId == null || course.getCategory().getId().equals(categoryId))
                .filter(course -> teacherId == null || course.getTeacher().getId().equals(teacherId))
                .filter(course -> tagId == null || course.getTags().stream().anyMatch(tag -> tag.getId().equals(tagId)))
                .map(CourseMapper::toSummary)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Course course = requireCourse(id);
        courseRepository.delete(course);
        log.info("Удалён курс id={}", id);
    }

    @Transactional
    public CourseDetailsResponse replaceTags(Long id, UpdateCourseTagsRequest request) {
        Course course = requireCourse(id);
        replaceTags(course, request.tagIds());
        log.info("Обновлены теги курса id={}", id);
        return CourseMapper.toDetails(course);
    }

    public Course requireCourse(Long id) {
        return Lookups.required(courseRepository.findById(id), "Курс с id=" + id + " не найден");
    }

    private void apply(
            Course course,
            String title,
            String description,
            Long categoryId,
            Long teacherId,
            String duration,
            java.time.LocalDate startDate
    ) {
        Category category = categoryService.requireCategory(categoryId);
        User teacher = userService.requireUser(teacherId);
        if (teacher.getRole() == Role.STUDENT) {
            throw new BusinessRuleException("Преподавателем курса может быть только пользователь с ролью TEACHER или ADMIN");
        }
        course.setTitle(title.trim());
        course.setDescription(Texts.trimToNull(description));
        course.setCategory(category);
        course.setTeacher(teacher);
        course.setDuration(Texts.trimToNull(duration));
        course.setStartDate(startDate);
    }

    private void replaceTags(Course course, List<Long> tagIds) {
        Set<Tag> tags = new HashSet<>();
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                tags.add(tagService.requireTag(tagId));
            }
        }
        course.getTags().clear();
        tags.forEach(course::addTag);
    }
}
