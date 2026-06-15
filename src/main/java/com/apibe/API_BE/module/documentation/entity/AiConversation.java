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
@Table(name = "ai_conversations")
public class AiConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "project_id", columnDefinition = "CHAR(36)")
    private UUID projectId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "title")
    private String title;

    @Column(name = "mode")
    private String mode;

    @Column(name = "is_pinned")
    private Boolean pinned;

    @Column(name = "is_archived")
    private Boolean archived;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
