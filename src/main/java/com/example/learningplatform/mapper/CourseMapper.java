package com.example.learningplatform.mapper;

import com.example.learningplatform.domain.Assignment;
import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Lesson;
import com.example.learningplatform.domain.Module;
import com.example.learningplatform.domain.Quiz;
import com.example.learningplatform.dto.response.AssignmentResponse;
import com.example.learningplatform.dto.response.CourseDetailsResponse;
import com.example.learningplatform.dto.response.CourseSummaryResponse;
import com.example.learningplatform.dto.response.LessonResponse;
import com.example.learningplatform.dto.response.ModuleResponse;
import com.example.learningplatform.dto.response.QuizSummaryResponse;
import com.example.learningplatform.dto.response.TagResponse;

import java.util.Comparator;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseSummaryResponse toSummary(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getDuration(),
                course.getStartDate(),
                course.getCategory().getId(),
                course.getCategory().getName(),
                course.getTeacher().getId(),
                course.getTeacher().getName(),
                course.getTags().stream()
                        .map(CatalogMapper::toTag)
                        .sorted(Comparator.comparing(TagResponse::name, String.CASE_INSENSITIVE_ORDER))
                        .toList());
    }

    public static CourseDetailsResponse toDetails(Course course) {
        return new CourseDetailsResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getDuration(),
                course.getStartDate(),
                CatalogMapper.toCategory(course.getCategory()),
                UserMapper.toSummary(course.getTeacher()),
                course.getTags().stream()
                        .map(CatalogMapper::toTag)
                        .sorted(Comparator.comparing(TagResponse::name, String.CASE_INSENSITIVE_ORDER))
                        .toList(),
                course.getModules().stream()
                        .sorted(Comparator.comparingInt(Module::getOrderIndex))
                        .map(CourseMapper::toModule)
                        .toList());
    }

    public static ModuleResponse toModule(Module module) {
        Quiz quiz = module.getQuiz();
        return new ModuleResponse(
                module.getId(),
                module.getCourse().getId(),
                module.getTitle(),
                module.getOrderIndex(),
                module.getDescription(),
                module.getLessons().stream().map(CourseMapper::toLesson).toList(),
                quiz == null ? null : new QuizSummaryResponse(quiz.getId(), quiz.getTitle(), quiz.getTimeLimit()));
    }

    public static LessonResponse toLesson(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getModule().getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getVideoUrl(),
                lesson.getAssignments().stream().map(CourseMapper::toAssignment).toList());
    }

    public static AssignmentResponse toAssignment(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getLesson().getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getMaxScore());
    }
}
