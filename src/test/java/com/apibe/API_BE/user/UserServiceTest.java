package com.apibe.API_BE.user;

import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.security.CustomUserDetails;
import com.apibe.API_BE.module.user.dto.request.UpdateSettingRequest;
import com.apibe.API_BE.module.user.dto.response.UserSettingResponse;
import com.apibe.API_BE.module.user.entity.User;
import com.apibe.API_BE.module.user.entity.UserSetting;
import com.apibe.API_BE.module.user.repository.UserRepository;
import com.apibe.API_BE.module.user.repository.UserSettingRepository;
import com.apibe.API_BE.module.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingRepository userSettingRepository;

    private UserService userService;
    private UUID mockUserId;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userSettingRepository, new ObjectMapper());
        mockUserId = UUID.randomUUID();

        User mockUser = new User();
        mockUser.setId(mockUserId);
        mockUser.setUsername("testuser");
        mockUser.setRole(UserRole.USER);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetOrCreateUserSettingWhenNotExists() {
        User mockUser = new User();
        mockUser.setId(mockUserId);
        mockUser.setUsername("testuser");

        when(userSettingRepository.findByUserId(mockUserId)).thenReturn(Optional.empty());
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        when(userSettingRepository.saveAndFlush(any(UserSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettingResponse response = userService.getUserSetting();

        assertNotNull(response);
        assertEquals("vi", response.getLanguage());
        assertEquals("{}", response.getNotificationSettings().toString());
        assertEquals("{}", response.getPrivacySettings().toString());
    }

    @Test
    void testUpdateUserSettingWithJsonMap() {
        UserSetting existingSetting = new UserSetting();
        existingSetting.setId(UUID.randomUUID());
        existingSetting.setLanguage("vi");
        existingSetting.setNotificationSettings("{}");
        existingSetting.setPrivacySettings("{}");

        when(userSettingRepository.findByUserId(mockUserId)).thenReturn(Optional.of(existingSetting));
        when(userSettingRepository.saveAndFlush(any(UserSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> newNotification = new HashMap<>();
        newNotification.put("email", true);
        newNotification.put("push", false);

        UpdateSettingRequest request = UpdateSettingRequest.builder()
                .language("en")
                .notificationSettings(newNotification)
                .build();

        UserSettingResponse response = userService.updateUserSetting(request);

        assertNotNull(response);
        assertEquals("en", response.getLanguage());
        assertTrue(response.getNotificationSettings().toString().contains("email"));
        assertTrue(response.getNotificationSettings().toString().contains("true"));
        verify(userSettingRepository).saveAndFlush(existingSetting);
    }
}
