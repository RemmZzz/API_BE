package com.apibe.API_BE.module.user.repository;

import com.apibe.API_BE.module.user.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
}

