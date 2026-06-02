package com.apibe.API_BE.module.database.repository;

import com.apibe.API_BE.module.database.entity.DatabaseSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatabaseSchemaRepository extends JpaRepository<DatabaseSchema, UUID> {
}

