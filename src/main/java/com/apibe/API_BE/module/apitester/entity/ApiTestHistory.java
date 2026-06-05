package com.apibe.API_BE.module.apitester.entity;

import com.apibe.API_BE.global.enums.HttpMethodType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "api_test_history")
public class ApiTestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "project_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID projectId;

    @Column(name = "name", length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private HttpMethodType method;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    /** JSON string stored as LONGTEXT */
    @Column(name = "request_headers", columnDefinition = "LONGTEXT")
    private String requestHeaders;

    /** JSON string stored as LONGTEXT */
    @Column(name = "request_params", columnDefinition = "LONGTEXT")
    private String requestParams;

    /** JSON string stored as LONGTEXT */
    @Column(name = "request_body", columnDefinition = "LONGTEXT")
    private String requestBody;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "status_text", length = 100)
    private String statusText;

    /** JSON string stored as LONGTEXT */
    @Column(name = "response_headers", columnDefinition = "LONGTEXT")
    private String responseHeaders;

    /** JSON string stored as LONGTEXT */
    @Column(name = "response_body", columnDefinition = "LONGTEXT")
    private String responseBody;

    @Column(name = "duration_ms")
    private Long durationMs;

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
