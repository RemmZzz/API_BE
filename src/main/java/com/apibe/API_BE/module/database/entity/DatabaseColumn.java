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
@Table(name = "database_columns")
public class DatabaseColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "table_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID tableId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "data_type", nullable = false, length = 100)
    private String dataType;

    @Column(name = "length")
    private Integer length;

    @Column(name = "precision_value")
    private Integer precisionValue;

    @Column(name = "scale_value")
    private Integer scaleValue;

    @Column(name = "is_primary_key", nullable = false)
    private boolean isPrimaryKey;

    @Column(name = "is_nullable", nullable = false)
    private boolean isNullable;

    @Column(name = "is_unique", nullable = false)
    private boolean isUnique;

    @Column(name = "is_auto_increment", nullable = false)
    private boolean isAutoIncrement;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "ordinal_position", nullable = false)
    private int ordinalPosition;

    @Column(name = "comment")
    private String comment;

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
