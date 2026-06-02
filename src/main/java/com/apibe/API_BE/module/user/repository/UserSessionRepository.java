package com.apibe.API_BE.module.user.repository;

import com.apibe.API_BE.module.user.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
}

