package com.example.learningplatform.dto.response;

import com.example.learningplatform.enums.Role;

public record UserSummaryResponse(
        Long id,
        String name,
        String email,
        Role role
) {
}
