package com.example.learningplatform.repository;

import com.example.learningplatform.domain.Submission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @EntityGraph(attributePaths = {"assignment", "student"})
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @EntityGraph(attributePaths = {"assignment", "student"})
    List<Submission> findByAssignmentId(Long assignmentId);

    @EntityGraph(attributePaths = {"assignment", "student"})
    List<Submission> findByStudentId(Long studentId);

    @Override
    @EntityGraph(attributePaths = {"assignment", "student"})
    Optional<Submission> findById(Long id);
}
