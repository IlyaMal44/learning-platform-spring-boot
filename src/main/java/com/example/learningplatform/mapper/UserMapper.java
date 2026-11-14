package com.example.learningplatform.mapper;

import com.example.learningplatform.domain.Profile;
import com.example.learningplatform.domain.User;
import com.example.learningplatform.dto.response.ProfileResponse;
import com.example.learningplatform.dto.response.UserResponse;
import com.example.learningplatform.dto.response.UserSummaryResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                toProfile(user.getProfile()));
    }

    public static ProfileResponse toProfile(Profile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileResponse(profile.getId(), profile.getBio(), profile.getAvatarUrl());
    }
}
