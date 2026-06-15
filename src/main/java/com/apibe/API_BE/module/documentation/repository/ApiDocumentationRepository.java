package com.apibe.API_BE.module.documentation.repository;

import com.apibe.API_BE.module.documentation.entity.ApiDocumentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiDocumentationRepository extends JpaRepository<ApiDocumentation, UUID> {
    Optional<ApiDocumentation> findByProjectId(UUID projectId);
}
