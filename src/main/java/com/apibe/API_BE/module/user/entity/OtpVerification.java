package com.apibe.API_BE.module.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "otp_verifications")
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu ID thành String
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu User ID thành String
    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "purpose", nullable = false, length = 50)
    private String purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}