package com.apibe.API_BE.module.user.dto.response;

import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private String name;
    private UserRole role;
    private UserStatus status;
    private String phone;
    private String avatarUrl;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String plan;
}
