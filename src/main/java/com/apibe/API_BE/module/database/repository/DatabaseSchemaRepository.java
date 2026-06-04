package com.apibe.API_BE.module.database.repository;

import com.apibe.API_BE.module.database.entity.DatabaseSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DatabaseSchemaRepository extends JpaRepository<DatabaseSchema, UUID> {
    Optional<DatabaseSchema> findByProjectId(UUID projectId);
    Optional<DatabaseSchema> findByProjectIdAndName(UUID projectId, String name);
}
