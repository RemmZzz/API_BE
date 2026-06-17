package com.apibe.API_BE.module.apitester.entity;

import com.apibe.API_BE.global.enums.HttpMethodType;
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
@Table(name = "api_test_history") // Đã sửa thành số ít để khớp với log database lúc đầu
public class ApiTestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu ID
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu Project ID
    @Column(name = "project_id", columnDefinition = "CHAR(36)")
    private UUID projectId;

    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu User ID
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "name", length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private HttpMethodType method;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "request_headers", columnDefinition = "LONGTEXT")
    private String requestHeaders;

    @Column(name = "request_params", columnDefinition = "LONGTEXT")
    private String requestParams;

    @Column(name = "request_body", columnDefinition = "LONGTEXT")
    private String requestBody;

    // Đã đổi responseStatus thành statusCode để khớp với Service
    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "status_text", length = 100)
    private String statusText;

    @Column(name = "response_headers", columnDefinition = "LONGTEXT")
    private String responseHeaders;

    @Column(name = "response_body", columnDefinition = "LONGTEXT")
    private String responseBody;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "response_size_bytes")
    private Long responseSizeBytes;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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