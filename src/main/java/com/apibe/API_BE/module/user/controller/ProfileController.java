package com.apibe.API_BE.module.user.controller;

import com.apibe.API_BE.module.user.dto.request.ChangePasswordRequest;
import com.apibe.API_BE.module.user.dto.request.UpdateProfileRequest;
import com.apibe.API_BE.module.user.dto.response.ProfileResponse;
import com.apibe.API_BE.module.user.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    @PatchMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
