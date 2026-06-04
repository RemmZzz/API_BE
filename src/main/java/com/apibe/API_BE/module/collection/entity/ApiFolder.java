package com.apibe.API_BE.module.collection.entity;

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
@Table(name = "api_folders")
public class ApiFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "collection_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID collectionId;

    @Column(name = "parent_folder_id", columnDefinition = "CHAR(36)")
    private UUID parentFolderId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "ordinal_position", nullable = false)
    private int ordinalPosition;

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
