package com.example.learningplatform.repository;

import com.example.learningplatform.domain.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByTeacherId(Long teacherId);

    boolean existsByCategoryId(Long categoryId);

    List<Course> findByTagsId(Long tagId);

    @Override
    @EntityGraph(attributePaths = {"category", "teacher", "tags"})
    Optional<Course> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"category", "teacher", "tags"})
    List<Course> findAll();
}
