package com.apibe.API_BE.module.user.repository;

import com.apibe.API_BE.module.user.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUserIdAndRevokedAtIsNullAndExpiresAtAfter(UUID userId, LocalDateTime now);
}

