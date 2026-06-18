package com.apibe.API_BE.module.auth.mapper;

import com.apibe.API_BE.module.auth.dto.response.UserProfileResponse;
import com.apibe.API_BE.module.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .status(user.getStatus())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

