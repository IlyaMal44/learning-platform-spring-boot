package com.example.learningplatform.service;

import com.example.learningplatform.exception.BusinessRuleException;
import com.example.learningplatform.repository.EnrollmentRepository;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentPolicy {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentPolicy(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public void requireEnrolled(Long studentId, Long courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessRuleException("Студент не записан на курс");
        }
    }
}
