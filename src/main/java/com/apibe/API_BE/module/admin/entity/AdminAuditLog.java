package com.apibe.API_BE.module.admin.entity;

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
@Table(name = "admin_audit_logs")
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "admin_id", columnDefinition = "CHAR(36)")
    private UUID adminId;

    @Column(name = "target_user_id", columnDefinition = "CHAR(36)")
    private UUID targetUserId;

    @Column(name = "action")
    private String action;

    @Column(name = "description")
    private String description;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
