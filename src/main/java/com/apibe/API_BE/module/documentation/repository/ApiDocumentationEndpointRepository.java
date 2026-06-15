package com.apibe.API_BE.module.documentation.repository;

import com.apibe.API_BE.module.documentation.entity.ApiDocumentationEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApiDocumentationEndpointRepository extends JpaRepository<ApiDocumentationEndpoint, UUID> {
}
