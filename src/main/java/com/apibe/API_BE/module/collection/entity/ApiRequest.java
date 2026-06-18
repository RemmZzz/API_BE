package com.apibe.API_BE.module.collection.entity;

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
@Table(name = "api_requests")
public class ApiRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu UUID
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu UUID
    @Column(name = "collection_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID collectionId;

    @JdbcTypeCode(SqlTypes.VARCHAR) // <--- Ép kiểu UUID
    @Column(name = "folder_id", columnDefinition = "CHAR(36)")
    private UUID folderId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private HttpMethodType method;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "description")
    private String description;

    // ĐÃ SỬA: headersJson -> headers
    @Column(name = "headers", columnDefinition = "LONGTEXT")
    private String headers;

    // ĐÃ SỬA: paramsJson -> params
    @Column(name = "params", columnDefinition = "LONGTEXT")
    private String params;

    @Column(name = "body", columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "body_type", length = 50)
    private String bodyType;

    @Column(name = "response_example", columnDefinition = "LONGTEXT")
    private String responseExample;

    // ĐÃ SỬA: ordinalPosition -> sortOrder
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.method == null) {
            this.method = HttpMethodType.GET;
        }
        if (this.bodyType == null) {
            this.bodyType = "json";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}