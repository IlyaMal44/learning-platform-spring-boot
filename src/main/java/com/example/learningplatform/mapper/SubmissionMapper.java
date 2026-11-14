package com.example.learningplatform.mapper;

import com.example.learningplatform.domain.Submission;
import com.example.learningplatform.dto.response.SubmissionResponse;

public final class SubmissionMapper {

    private SubmissionMapper() {
    }

    public static SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                submission.getStudent().getId(),
                submission.getStudent().getName(),
                submission.getSubmittedAt(),
                submission.getContent(),
                submission.getScore(),
                submission.getFeedback());
    }
}
