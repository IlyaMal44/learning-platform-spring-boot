package com.example.learningplatform.service;

import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.Enrollment;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.request.UpdateEnrollmentRequest;
import com.example.learningplatform.dto.response.EnrollmentResponse;
import com.example.learningplatform.dto.response.UserSummaryResponse;
import com.example.learningplatform.enums.EnrollmentStatus;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.EnrollmentMapper;
import com.example.learningplatform.mapper.UserMapper;
import com.example.learningplatform.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final CourseService courseService;
    private final Clock clock;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            UserService userService,
            CourseService courseService,
            Clock clock
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.courseService = courseService;
        this.clock = clock;
    }

    @Transactional
    public EnrollmentResponse enroll(Long courseId, Long studentId) {
        Course course = courseService.requireCourse(courseId);
        User student = userService.requireUser(studentId);
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleException("Записаться на курс может только студент");
        }
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new ConflictException("Студент уже записан на этот курс");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrolledAt(LocalDate.now(clock));
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Студент id={} записан на курс id={}", studentId, courseId);
        return EnrollmentMapper.toResponse(saved);
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        courseService.requireCourse(courseId);
        userService.requireUser(studentId);
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new com.example.learningplatform.exception.NotFoundException(
                        "Запись студента id=" + studentId + " на курс id=" + courseId + " не найдена"));
        enrollmentRepository.delete(enrollment);
        log.info("Студент id={} отписан от курса id={}", studentId, courseId);
    }

    public List<EnrollmentResponse> listByCourse(Long courseId) {
        courseService.requireCourse(courseId);
        return enrollmentRepository.findByCourseId(courseId).stream().map(EnrollmentMapper::toResponse).toList();
    }

    public List<EnrollmentResponse> listByStudent(Long studentId) {
        userService.requireUser(studentId);
        return enrollmentRepository.findByStudentId(studentId).stream().map(EnrollmentMapper::toResponse).toList();
    }

    public List<UserSummaryResponse> listStudents(Long courseId) {
        courseService.requireCourse(courseId);
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(enrollment -> UserMapper.toSummary(enrollment.getStudent()))
                .toList();
    }

    @Transactional
    public EnrollmentResponse updateStatus(Long id, UpdateEnrollmentRequest request) {
        Enrollment enrollment = Lookups.required(
                enrollmentRepository.findById(id),
                "Запись на курс с id=" + id + " не найдена");
        enrollment.setStatus(request.status());
        log.info("Статус записи id={} изменён на {}", id, request.status());
        return EnrollmentMapper.toResponse(enrollment);
    }
}
