package com.apibe.API_BE.module.documentation.entity;

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
@Table(name = "api_documentation_endpoints")
public class ApiDocumentationEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentation_id", nullable = false)
    private ApiDocumentation documentation;

    @Column(name = "method", nullable = false, length = 20)
    private String method;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "headers_json", columnDefinition = "LONGTEXT")
    private String headersJson;

    @Column(name = "params_json", columnDefinition = "LONGTEXT")
    private String paramsJson;

    @Column(name = "body_example", columnDefinition = "LONGTEXT")
    private String bodyExample;

    @Column(name = "response_example", columnDefinition = "LONGTEXT")
    private String responseExample;

    @Column(name = "error_example", columnDefinition = "LONGTEXT")
    private String errorExample;

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
