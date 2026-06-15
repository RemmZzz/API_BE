package com.apibe.API_BE.module.environment.repository;

import com.apibe.API_BE.module.environment.entity.EnvironmentVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariable, UUID> {
    List<EnvironmentVariable> findByEnvironmentId(UUID environmentId);
}
