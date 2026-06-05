package com.apibe.API_BE.module.collection.entity;

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
@Table(name = "api_requests")
public class ApiRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "collection_id", nullable = false, columnDefinition = "CHAR(36)")
    private UUID collectionId;

    @Column(name = "folder_id", columnDefinition = "CHAR(36)")
    private UUID folderId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    @Builder.Default
    private HttpMethodType method = HttpMethodType.GET;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    /** JSON string stored as LONGTEXT */
    @Column(name = "headers", columnDefinition = "LONGTEXT")
    private String headers;

    /** JSON string stored as LONGTEXT */
    @Column(name = "params", columnDefinition = "LONGTEXT")
    private String params;

    /** JSON string stored as LONGTEXT */
    @Column(name = "body", columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

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
