package com.apibe.API_BE.module.database.repository;

import com.apibe.API_BE.module.database.entity.DatabaseRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DatabaseRelationshipRepository extends JpaRepository<DatabaseRelationship, UUID> {
    List<DatabaseRelationship> findBySchemaId(UUID schemaId);
}
