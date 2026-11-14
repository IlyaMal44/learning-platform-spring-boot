package com.example.learningplatform.service;

import com.example.learningplatform.domain.Assignment;
import com.example.learningplatform.domain.Lesson;
import com.example.learningplatform.domain.Submission;
import com.example.learningplatform.dto.request.CreateAssignmentRequest;
import com.example.learningplatform.dto.request.UpdateAssignmentRequest;
import com.example.learningplatform.dto.response.AssignmentResponse;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.mapper.CourseMapper;
import com.example.learningplatform.repository.AssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentService.class);

    private final AssignmentRepository assignmentRepository;
    private final LessonService lessonService;

    public AssignmentService(AssignmentRepository assignmentRepository, LessonService lessonService) {
        this.assignmentRepository = assignmentRepository;
        this.lessonService = lessonService;
    }

    @Transactional
    public AssignmentResponse create(Long lessonId, CreateAssignmentRequest request) {
        Lesson lesson = lessonService.requireLesson(lessonId);
        Assignment assignment = new Assignment();
        assignment.setLesson(lesson);
        apply(assignment, request.title(), request.description(), request.dueDate(), request.maxScore());
        Assignment saved = assignmentRepository.save(assignment);
        log.info("Создано задание id={} в уроке id={}", saved.getId(), lessonId);
        return CourseMapper.toAssignment(saved);
    }

    @Transactional
    public AssignmentResponse update(Long id, UpdateAssignmentRequest request) {
        Assignment assignment = requireAssignment(id);
        boolean scoreTooHigh = assignment.getSubmissions().stream()
                .map(Submission::getScore)
                .anyMatch(score -> score != null && score > request.maxScore());
        if (scoreTooHigh) {
            throw new BusinessRuleException("Нельзя уменьшить максимальный балл ниже уже выставленной оценки");
        }
        apply(assignment, request.title(), request.description(), request.dueDate(), request.maxScore());
        log.info("Обновлено задание id={}", id);
        return CourseMapper.toAssignment(assignment);
    }

    public AssignmentResponse get(Long id) {
        return CourseMapper.toAssignment(requireAssignment(id));
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = requireAssignment(id);
        assignmentRepository.delete(assignment);
        log.info("Удалено задание id={}", id);
    }

    public Assignment requireAssignment(Long id) {
        return Lookups.required(assignmentRepository.findById(id), "Задание с id=" + id + " не найдено");
    }

    private void apply(Assignment assignment, String title, String description, java.time.LocalDate dueDate, int maxScore) {
        assignment.setTitle(title.trim());
        assignment.setDescription(Texts.trimToNull(description));
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(maxScore);
    }
}
