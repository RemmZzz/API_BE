package com.apibe.API_BE.module.user.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.module.user.dto.request.UpdateSettingRequest;
import com.apibe.API_BE.module.user.dto.response.UserSettingResponse;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.entity.UserSetting;
import com.apibe.API_BE.module.user.repository.UserRepository;
import com.apibe.API_BE.module.user.repository.UserSettingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final ObjectMapper objectMapper;

    public UserService(UserRepository userRepository, UserSettingRepository userSettingRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.userSettingRepository = userSettingRepository;
        this.objectMapper = objectMapper;
    }

    public UserSettingResponse getUserSetting() {
        UUID userId = SecurityUtils.getCurrentUserId();
        UserSetting setting = getOrCreateUserSetting(userId);

        return UserSettingResponse.builder()
                .language(setting.getLanguage())
                .notificationSettings(parseJson(setting.getNotificationSettings()))
                .privacySettings(parseJson(setting.getPrivacySettings()))
                .build();
    }

    @Transactional
    public UserSettingResponse updateUserSetting(UpdateSettingRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        UserSetting setting = getOrCreateUserSetting(userId);

        if (request.getLanguage() != null) {
            setting.setLanguage(request.getLanguage());
        }
        if (request.getNotificationSettings() != null) {
            setting.setNotificationSettings(toJsonString(request.getNotificationSettings()));
        }
        if (request.getPrivacySettings() != null) {
            setting.setPrivacySettings(toJsonString(request.getPrivacySettings()));
        }

        userSettingRepository.saveAndFlush(setting);

        return UserSettingResponse.builder()
                .language(setting.getLanguage())
                .notificationSettings(parseJson(setting.getNotificationSettings()))
                .privacySettings(parseJson(setting.getPrivacySettings()))
                .build();
    }

    private UserSetting getOrCreateUserSetting(UUID userId) {
        return userSettingRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            UserSetting newSetting = UserSetting.builder()
                    .user(user)
                    .language("vi")
                    .notificationSettings("{}")
                    .privacySettings("{}")
                    .build();

            return userSettingRepository.saveAndFlush(newSetting);
        });
    }

    private Object parseJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            return jsonStr;
        }
    }

    private String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String stringVal) {
            return stringVal;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
