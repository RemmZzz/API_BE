package com.apibe.API_BE.module.database.repository;

import com.apibe.API_BE.module.database.entity.DatabaseTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatabaseTableRepository extends JpaRepository<DatabaseTable, UUID> {
}

