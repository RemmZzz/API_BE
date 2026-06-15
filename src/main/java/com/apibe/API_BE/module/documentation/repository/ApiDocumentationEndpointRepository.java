package com.apibe.API_BE.module.documentation.repository;

import com.apibe.API_BE.module.documentation.entity.ApiDocumentationEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiDocumentationEndpointRepository extends JpaRepository<ApiDocumentationEndpoint, UUID> {
}
