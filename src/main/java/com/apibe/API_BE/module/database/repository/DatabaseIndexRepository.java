package com.apibe.API_BE.module.database.repository;

import com.apibe.API_BE.module.database.entity.DatabaseIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatabaseIndexRepository extends JpaRepository<DatabaseIndex, UUID> {
}

