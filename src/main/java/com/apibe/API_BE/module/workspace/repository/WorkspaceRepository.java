package com.apibe.API_BE.module.workspace.repository;

import com.apibe.API_BE.module.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findByProjectId(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
