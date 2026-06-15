package com.apibe.API_BE.module.user.repository;

import com.apibe.API_BE.module.user.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSettingRepository extends JpaRepository<UserSetting, UUID> {
    Optional<UserSetting> findByUserId(UUID userId);
}
