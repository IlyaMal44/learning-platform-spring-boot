package com.example.learningplatform.service;

import com.example.learningplatform.domain.Assignment;
import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Submission;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.request.CreateSubmissionRequest;
import com.example.learningplatform.dto.request.GradeSubmissionRequest;
import com.example.learningplatform.dto.response.SubmissionResponse;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.SubmissionMapper;
import com.example.learningplatform.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final EnrollmentPolicy enrollmentPolicy;
    private final Clock clock;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            AssignmentService assignmentService,
            UserService userService,
            EnrollmentPolicy enrollmentPolicy,
            Clock clock
    ) {
        this.submissionRepository = submissionRepository;
        this.assignmentService = assignmentService;
        this.userService = userService;
        this.enrollmentPolicy = enrollmentPolicy;
        this.clock = clock;
    }

    @Transactional
    public SubmissionResponse submit(Long assignmentId, CreateSubmissionRequest request) {
        Assignment assignment = assignmentService.requireAssignment(assignmentId);
        User student = userService.requireUser(request.studentId());
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleException("Отправить решение может только студент");
        }
        Course course = assignment.getLesson().getModule().getCourse();
        enrollmentPolicy.requireEnrolled(student.getId(), course.getId());
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDate.now(clock))) {
            throw new BusinessRuleException("Срок сдачи задания истёк");
        }
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, student.getId())) {
            throw new ConflictException("Студент уже отправил решение по этому заданию");
        }
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(request.content().trim());
        submission.setSubmittedAt(LocalDateTime.now(clock));
        Submission saved = submissionRepository.save(submission);
        log.info("Студент id={} отправил решение id={} по заданию id={}", student.getId(), saved.getId(), assignmentId);
        return SubmissionMapper.toResponse(saved);
    }

    @Transactional
    public SubmissionResponse grade(Long submissionId, GradeSubmissionRequest request) {
        Submission submission = requireSubmission(submissionId);
        User teacher = userService.requireUser(request.teacherId());
        Course course = submission.getAssignment().getLesson().getModule().getCourse();
        boolean allowed = teacher.getRole() == Role.ADMIN || course.getTeacher().getId().equals(teacher.getId());
        if (!allowed) {
            throw new BusinessRuleException("Оценивать работу может преподаватель этого курса или администратор");
        }
        if (request.score() > submission.getAssignment().getMaxScore()) {
            throw new BusinessRuleException("Оценка не может быть больше максимального балла " + submission.getAssignment().getMaxScore());
        }
        submission.setScore(request.score());
        submission.setFeedback(Texts.trimToNull(request.feedback()));
        log.info("Решение id={} оценено на {} баллов", submissionId, request.score());
        return SubmissionMapper.toResponse(submission);
    }

    public SubmissionResponse get(Long id) {
        return SubmissionMapper.toResponse(requireSubmission(id));
    }

    public List<SubmissionResponse> listByAssignment(Long assignmentId) {
        assignmentService.requireAssignment(assignmentId);
        return submissionRepository.findByAssignmentId(assignmentId).stream().map(SubmissionMapper::toResponse).toList();
    }

    public List<SubmissionResponse> listByStudent(Long studentId) {
        userService.requireUser(studentId);
        return submissionRepository.findByStudentId(studentId).stream().map(SubmissionMapper::toResponse).toList();
    }

    private Submission requireSubmission(Long id) {
        return Lookups.required(submissionRepository.findById(id), "Решение с id=" + id + " не найдено");
    }
}
