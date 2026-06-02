package com.apibe.API_BE.module.user.repository;

import com.apibe.API_BE.module.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
}

