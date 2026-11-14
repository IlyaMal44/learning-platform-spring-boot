package com.example.learningplatform.service;

import com.example.learningplatform.domain.Course;
import com.example.learningplatform.domain.CourseReview;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.request.CreateReviewRequest;
import com.example.learningplatform.dto.request.UpdateReviewRequest;
import com.example.learningplatform.dto.response.ReviewResponse;
import com.example.learningplatform.enums.Role;
import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.ReviewMapper;
import com.example.learningplatform.repository.CourseReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final CourseReviewRepository reviewRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final EnrollmentPolicy enrollmentPolicy;
    private final Clock clock;

    public ReviewService(
            CourseReviewRepository reviewRepository,
            CourseService courseService,
            UserService userService,
            EnrollmentPolicy enrollmentPolicy,
            Clock clock
    ) {
        this.reviewRepository = reviewRepository;
        this.courseService = courseService;
        this.userService = userService;
        this.enrollmentPolicy = enrollmentPolicy;
        this.clock = clock;
    }

    @Transactional
    public ReviewResponse create(Long courseId, CreateReviewRequest request) {
        Course course = courseService.requireCourse(courseId);
        User student = userService.requireUser(request.studentId());
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessRuleException("Отзыв может оставить только студент");
        }
        enrollmentPolicy.requireEnrolled(student.getId(), course.getId());
        if (reviewRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
            throw new ConflictException("Студент уже оставил отзыв на этот курс");
        }
        CourseReview review = new CourseReview();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(request.rating());
        review.setComment(Texts.trimToNull(request.comment()));
        review.setCreatedAt(LocalDateTime.now(clock));
        CourseReview saved = reviewRepository.save(review);
        log.info("Студент id={} оставил отзыв id={} на курс id={}", student.getId(), saved.getId(), courseId);
        return ReviewMapper.toResponse(saved);
    }

    @Transactional
    public ReviewResponse update(Long id, UpdateReviewRequest request) {
        CourseReview review = requireReview(id);
        review.setRating(request.rating());
        review.setComment(Texts.trimToNull(request.comment()));
        log.info("Обновлён отзыв id={}", id);
        return ReviewMapper.toResponse(review);
    }

    public ReviewResponse get(Long id) {
        return ReviewMapper.toResponse(requireReview(id));
    }

    public List<ReviewResponse> listByCourse(Long courseId) {
        courseService.requireCourse(courseId);
        return reviewRepository.findByCourseId(courseId).stream().map(ReviewMapper::toResponse).toList();
    }

    public List<ReviewResponse> listByStudent(Long studentId) {
        userService.requireUser(studentId);
        return reviewRepository.findByStudentId(studentId).stream().map(ReviewMapper::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        CourseReview review = requireReview(id);
        reviewRepository.delete(review);
        log.info("Удалён отзыв id={}", id);
    }

    private CourseReview requireReview(Long id) {
        return Lookups.required(reviewRepository.findById(id), "Отзыв с id=" + id + " не найден");
    }
}
