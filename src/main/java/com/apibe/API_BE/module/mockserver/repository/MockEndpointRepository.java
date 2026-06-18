package com.apibe.API_BE.module.mockserver.repository;

import com.apibe.API_BE.global.enums.HttpMethodType;
import com.apibe.API_BE.module.mockserver.entity.MockEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MockEndpointRepository extends JpaRepository<MockEndpoint, UUID> {
    List<MockEndpoint> findByProjectId(UUID projectId);
    Optional<MockEndpoint> findByProjectIdAndMethodAndPath(UUID projectId, HttpMethodType method, String path);
    List<MockEndpoint> findByProjectIdAndMethodAndIsEnabledTrue(UUID projectId, HttpMethodType method);
}
