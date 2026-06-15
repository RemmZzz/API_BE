package com.apibe.API_BE.module.documentation.repository;

import com.apibe.API_BE.module.documentation.entity.ApiDocumentation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiDocumentationRepository extends JpaRepository<ApiDocumentation, UUID> {
    Optional<ApiDocumentation> findByProjectId(UUID projectId);
}
