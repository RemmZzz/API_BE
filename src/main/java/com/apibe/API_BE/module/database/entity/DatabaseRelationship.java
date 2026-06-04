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
@Table(name = "database_relationships")
public class DatabaseRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "schema_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID schemaId;

    @Column(name = "source_table_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID sourceTableId;

    @Column(name = "source_column_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID sourceColumnId;

    @Column(name = "target_table_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID targetTableId;

    @Column(name = "target_column_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID targetColumnId;

    @Column(name = "constraint_name", length = 150)
    private String constraintName;

    @Column(name = "on_delete_action", length = 50)
    private String onDeleteAction;

    @Column(name = "on_update_action", length = 50)
    private String onUpdateAction;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.onDeleteAction == null) {
            this.onDeleteAction = "NO ACTION";
        }
        if (this.onUpdateAction == null) {
            this.onUpdateAction = "NO ACTION";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
