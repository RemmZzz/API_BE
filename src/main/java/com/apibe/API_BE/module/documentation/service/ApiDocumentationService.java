package com.apibe.API_BE.module.documentation.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.collection.entity.ApiCollection;
import com.apibe.API_BE.module.collection.entity.ApiRequest;
import com.apibe.API_BE.module.collection.repository.ApiCollectionRepository;
import com.apibe.API_BE.module.collection.repository.ApiRequestRepository;
import com.apibe.API_BE.module.documentation.dto.request.SaveApiDocumentationEndpointRequest;
import com.apibe.API_BE.module.documentation.dto.request.SaveApiDocumentationRequest;
import com.apibe.API_BE.module.documentation.dto.response.ApiDocumentationResponse;
import com.apibe.API_BE.module.documentation.entity.ApiDocumentation;
import com.apibe.API_BE.module.documentation.entity.ApiDocumentationEndpoint;
import com.apibe.API_BE.module.documentation.mapper.ApiDocumentationMapper;
import com.apibe.API_BE.module.documentation.repository.ApiDocumentationRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ApiDocumentationService {

    private final ApiDocumentationRepository apiDocumentationRepository;
    private final ProjectRepository projectRepository;
    private final ApiCollectionRepository apiCollectionRepository;
    private final ApiRequestRepository apiRequestRepository;
    private final ApiDocumentationMapper mapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ApiDocumentationResponse getDocumentation(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }

        return apiDocumentationRepository.findByProjectId(projectId)
                .map(mapper::toResponse)
                .orElseGet(() -> ApiDocumentationResponse.builder()
                        .projectId(projectId)
                        .title("API Documentation")
                        .version("1.0.0")
                        .endpoints(Collections.emptyList())
                        .updatedAt(null)
                        .build());
    }

    @Transactional
    public ApiDocumentationResponse saveDocumentation(UUID projectId, SaveApiDocumentationRequest request) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }

        ApiDocumentation doc = apiDocumentationRepository.findByProjectId(projectId)
                .orElseGet(() -> ApiDocumentation.builder()
                        .projectId(projectId)
                        .build());

        doc.setTitle(request.getTitle() != null ? request.getTitle() : "API Documentation");
        doc.setVersion(request.getVersion() != null ? request.getVersion() : "1.0.0");
        doc.setUpdatedAt(LocalDateTime.now());

        List<ApiDocumentationEndpoint> endpoints = new ArrayList<>();
        if (request.getEndpoints() != null) {
            for (SaveApiDocumentationEndpointRequest epReq : request.getEndpoints()) {
                String headersStr = null;
                String paramsStr = null;
                try {
                    if (epReq.getHeaders() != null) {
                        headersStr = objectMapper.writeValueAsString(epReq.getHeaders());
                    }
                    if (epReq.getParams() != null) {
                        paramsStr = objectMapper.writeValueAsString(epReq.getParams());
                    }
                } catch (Exception e) {
                    // ignore
                }

                ApiDocumentationEndpoint ep = ApiDocumentationEndpoint.builder()
                        .documentation(doc)
                        .method(epReq.getMethod() != null ? epReq.getMethod() : "GET")
                        .url(epReq.getUrl() != null ? epReq.getUrl() : "/endpoint")
                        .description(epReq.getDescription())
                        .headersJson(headersStr)
                        .paramsJson(paramsStr)
                        .bodyExample(epReq.getBodyExample())
                        .responseExample(epReq.getResponseExample())
                        .errorExample(epReq.getErrorExample() != null ? epReq.getErrorExample() : "{\n  \"message\": \"Request failed\"\n}")
                        .build();

                if (epReq.getId() != null && !epReq.getId().isEmpty()) {
                    try {
                        ep.setId(UUID.fromString(epReq.getId()));
                    } catch (IllegalArgumentException e) {
                        // Let JPA generate UUID
                    }
                }
                endpoints.add(ep);
            }
        }

        doc.getEndpoints().clear();
        doc.getEndpoints().addAll(endpoints);

        ApiDocumentation saved = apiDocumentationRepository.save(doc);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ApiDocumentationResponse generateDocumentationFromCollections(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }

        ApiDocumentation doc = apiDocumentationRepository.findByProjectId(projectId)
                .orElseGet(() -> ApiDocumentation.builder()
                        .projectId(projectId)
                        .title("API Documentation")
                        .version("1.0.0")
                        .build());

        List<ApiCollection> collections = apiCollectionRepository.findByProjectId(projectId);
        List<ApiDocumentationEndpoint> endpoints = new ArrayList<>();

        for (ApiCollection col : collections) {
            List<ApiRequest> requests = apiRequestRepository.findByCollectionId(col.getId());
            for (ApiRequest req : requests) {
                String desc = req.getDescription();
                if (desc == null || desc.trim().isEmpty()) {
                    desc = "Endpoint " + (req.getName() != null && !req.getName().isEmpty() ? req.getName() : req.getUrl());
                }

                ApiDocumentationEndpoint ep = ApiDocumentationEndpoint.builder()
                        .documentation(doc)
                        .method(req.getMethod() != null ? req.getMethod().name() : "GET")
                        .url(req.getUrl() != null ? req.getUrl() : "/endpoint")
                        .description(desc)
                        .headersJson(req.getHeadersJson())
                        .paramsJson(req.getParamsJson())
                        .bodyExample(req.getBody())
                        .responseExample(req.getResponseExample() != null && !req.getResponseExample().isEmpty()
                                ? req.getResponseExample() : "{\n  \"success\": true\n}")
                        .errorExample("{\n  \"message\": \"Request failed\"\n}")
                        .build();
                endpoints.add(ep);
            }
        }

        doc.getEndpoints().clear();
        doc.getEndpoints().addAll(endpoints);
        doc.setUpdatedAt(LocalDateTime.now());

        ApiDocumentation saved = apiDocumentationRepository.save(doc);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public String exportMarkdown(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }

        ApiDocumentation doc = apiDocumentationRepository.findByProjectId(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Documentation not found"));

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(doc.getTitle() != null ? doc.getTitle() : "API Documentation").append("\n\n");

        if (doc.getEndpoints() != null) {
            for (ApiDocumentationEndpoint endpoint : doc.getEndpoints()) {
                sb.append("## ").append(endpoint.getMethod()).append(" ").append(endpoint.getUrl()).append("\n\n");
                if (endpoint.getDescription() != null && !endpoint.getDescription().isEmpty()) {
                    sb.append(endpoint.getDescription()).append("\n\n");
                }

                sb.append("### Headers\n");
                sb.append("```json\n");
                sb.append(toJsonPretty(endpoint.getHeadersJson())).append("\n");
                sb.append("```\n\n");

                sb.append("### Body Example\n");
                sb.append("```json\n");
                sb.append(endpoint.getBodyExample() != null && !endpoint.getBodyExample().isEmpty()
                        ? endpoint.getBodyExample() : "{}").append("\n");
                sb.append("```\n\n");
            }
        }

        return sb.toString();
    }

    private String toJsonPretty(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return "[]";
        }
        try {
            Object obj = objectMapper.readValue(jsonStr, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return jsonStr;
        }
    }
}
