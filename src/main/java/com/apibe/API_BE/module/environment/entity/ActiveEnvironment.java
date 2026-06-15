package com.apibe.API_BE.module.environment.entity;

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
@Table(name = "active_environments")
public class ActiveEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "project_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private UUID projectId;

    @Column(name = "environment_id", columnDefinition = "CHAR(36)")
    private UUID environmentId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
