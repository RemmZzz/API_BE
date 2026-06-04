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
@Table(name = "api_test_histories")
public class ApiTestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "project_id", columnDefinition = "CHAR(36)")
    private UUID projectId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private HttpMethodType method;

    @Column(name = "url")
    private String url;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "response_size_bytes")
    private Long responseSizeBytes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
