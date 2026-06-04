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
@Table(name = "environment_variables")
public class EnvironmentVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "environment_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID environmentId;

    @Column(name = "variable_key", nullable = false, length = 100)
    private String variableKey;

    @Column(name = "initial_value")
    private String initialValue;

    @Column(name = "current_value")
    private String currentValue;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "is_secret", nullable = false)
    private boolean isSecret;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.type == null) {
            this.type = "default";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
