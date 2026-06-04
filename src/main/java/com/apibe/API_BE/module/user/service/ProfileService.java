package com.apibe.API_BE.module.user.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.module.user.dto.request.ChangePasswordRequest;
import com.apibe.API_BE.module.user.dto.request.UpdateProfileRequest;
import com.apibe.API_BE.module.user.dto.response.ProfileResponse;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@SuppressWarnings("null")
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public ProfileResponse getProfile() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String plan = "Free";
        try {
            plan = jdbcTemplate.queryForObject(
                    "SELECT plan_name FROM subscriptions WHERE user_id = ? AND status = 'ACTIVE' LIMIT 1",
                    String.class,
                    userId.toString()
            );
        } catch (EmptyResultDataAccessException ignored) {
            // Fallback to Free if no active subscription is found
        } catch (DataAccessException ignored) {
            // Fallback to Free if the subscription module has not created its table yet
        }

        return ProfileResponse.builder()
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
                .plan(plan)
                .build();
    }

    @Transactional
    public ProfileResponse updateProfile(UpdateProfileRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Tên không được để trống");
            }
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.saveAndFlush(user);

        return getProfile();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Mật khẩu cũ không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.saveAndFlush(user);
    }
}
