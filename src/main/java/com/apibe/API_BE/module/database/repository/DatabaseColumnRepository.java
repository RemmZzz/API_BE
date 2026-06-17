package com.apibe.API_BE.module.database.repository;

import com.apibe.API_BE.module.database.entity.DatabaseColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DatabaseColumnRepository extends JpaRepository<DatabaseColumn, UUID> {
    List<DatabaseColumn> findByTableId(UUID tableId);
}
