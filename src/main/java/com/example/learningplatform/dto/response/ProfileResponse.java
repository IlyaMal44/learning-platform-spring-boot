package com.example.learningplatform.dto.response;

public record ProfileResponse(
        Long id,
        String bio,
        String avatarUrl
) {
}
