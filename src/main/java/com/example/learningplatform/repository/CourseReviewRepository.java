package com.example.learningplatform.repository;

import com.example.learningplatform.domain.CourseReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    Optional<CourseReview> findByCourseIdAndStudentId(Long courseId, Long studentId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<CourseReview> findByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<CourseReview> findByStudentId(Long studentId);

    @Override
    @EntityGraph(attributePaths = {"student", "course"})
    Optional<CourseReview> findById(Long id);
}
