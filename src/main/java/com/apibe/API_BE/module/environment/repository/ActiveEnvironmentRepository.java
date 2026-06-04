package com.apibe.API_BE.module.environment.repository;

import com.apibe.API_BE.module.environment.entity.ActiveEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActiveEnvironmentRepository extends JpaRepository<ActiveEnvironment, UUID> {
    Optional<ActiveEnvironment> findByProjectId(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
