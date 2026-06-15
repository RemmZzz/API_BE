package com.apibe.API_BE.module.database.entity;

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
@Table(name = "database_indexes")
public class DatabaseIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "table_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID tableId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "columns_json", columnDefinition = "json", nullable = false)
    private String columnsJson;

    @Column(name = "is_unique", nullable = false)
    private boolean isUnique;

    @Column(name = "index_type", length = 50)
    private String indexType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.indexType == null) {
            this.indexType = "BTREE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
