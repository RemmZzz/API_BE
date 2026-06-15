package com.apibe.API_BE.module.environment.repository;

import com.apibe.API_BE.module.environment.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnvironmentRepository extends JpaRepository<Environment, UUID> {
    List<Environment> findByProjectId(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
