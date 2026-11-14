package com.example.learningplatform.repository;

import com.example.learningplatform.domain.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findByStudentId(Long studentId);

    @Override
    @EntityGraph(attributePaths = {"student", "course"})
    Optional<Enrollment> findById(Long id);
}
