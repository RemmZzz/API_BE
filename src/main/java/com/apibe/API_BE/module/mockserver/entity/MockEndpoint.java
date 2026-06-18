package com.apibe.API_BE.module.mockserver.entity;

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
@Table(name = "mock_endpoints")
public class MockEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "project_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private HttpMethodType method;

    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "delay_ms", nullable = false)
    private int delayMs;

    @Column(name = "response_headers_json", columnDefinition = "JSON")
    private String responseHeadersJson;

    @Column(name = "response_body", columnDefinition = "LONGTEXT")
    private String responseBody;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.statusCode == 0) {
            this.statusCode = 200;
        }
        if (this.method == null) {
            this.method = HttpMethodType.GET;
        }
        this.isEnabled = true;
        this.path = normalizePath(this.path);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.path = normalizePath(this.path);
    }

    public static String normalizePath(String path) {
        if (path == null) {
            return "/";
        }
        String p = path.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        if (p.endsWith("/") && p.length() > 1) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }
}
