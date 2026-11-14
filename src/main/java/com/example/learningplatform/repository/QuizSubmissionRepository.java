package com.example.learningplatform.repository;

import com.example.learningplatform.domain.QuizSubmission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    @EntityGraph(attributePaths = {"quiz", "student"})
    List<QuizSubmission> findByQuizId(Long quizId);

    @EntityGraph(attributePaths = {"quiz", "student"})
    List<QuizSubmission> findByStudentId(Long studentId);
}
