package com.apibe.API_BE.module.user.controller;

import com.apibe.API_BE.module.user.dto.request.UpdateSettingRequest;
import com.apibe.API_BE.module.user.dto.response.UserSettingResponse;
import com.apibe.API_BE.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserSettingResponse> getUserSetting() {
        return ResponseEntity.ok(userService.getUserSetting());
    }

    @PatchMapping
    public ResponseEntity<UserSettingResponse> updateUserSetting(@Valid @RequestBody UpdateSettingRequest request) {
        return ResponseEntity.ok(userService.updateUserSetting(request));
    }
}
