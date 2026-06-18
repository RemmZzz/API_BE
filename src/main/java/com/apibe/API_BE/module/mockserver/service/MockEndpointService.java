package com.apibe.API_BE.module.mockserver.service;

import com.apibe.API_BE.global.enums.HttpMethodType;
import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.module.mockserver.dto.request.CreateMockEndpointRequest;
import com.apibe.API_BE.module.mockserver.dto.request.UpdateMockEndpointRequest;
import com.apibe.API_BE.module.mockserver.dto.response.MockEndpointResponse;
import com.apibe.API_BE.module.mockserver.entity.MockEndpoint;
import com.apibe.API_BE.module.mockserver.mapper.MockEndpointMapper;
import com.apibe.API_BE.module.mockserver.repository.MockEndpointRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MockEndpointService {

    private final ProjectRepository projectRepository;
    private final MockEndpointRepository mockEndpointRepository;
    private final MockEndpointMapper mapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Transactional(readOnly = true)
    public List<MockEndpointResponse> getMockEndpoints(UUID projectId) {
        validateProjectAccess(projectId);

        return mockEndpointRepository.findByProjectId(projectId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MockEndpointResponse createMockEndpoint(UUID projectId, CreateMockEndpointRequest request) {
        validateProjectAccess(projectId);

        String normalizedPath = MockEndpoint.normalizePath(request.getPath());
        mockEndpointRepository.findByProjectIdAndMethodAndPath(projectId, request.getMethod(), normalizedPath)
                .ifPresent(existing -> {
                    throw new AppException(ErrorCode.BAD_REQUEST, "Mock endpoint with this method and path already exists");
                });

        MockEndpoint entity = mapper.toEntity(request);
        entity.setProjectId(projectId);
        entity.setPath(normalizedPath);

        entity = mockEndpointRepository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public MockEndpointResponse updateMockEndpoint(UUID mockEndpointId, UpdateMockEndpointRequest request) {
        MockEndpoint entity = mockEndpointRepository.findById(mockEndpointId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Mock endpoint not found"));

        validateProjectAccess(entity.getProjectId());

        String newPath = request.getPath() != null ? MockEndpoint.normalizePath(request.getPath()) : entity.getPath();
        HttpMethodType newMethod = request.getMethod() != null ? request.getMethod() : entity.getMethod();

        mockEndpointRepository.findByProjectIdAndMethodAndPath(entity.getProjectId(), newMethod, newPath)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(mockEndpointId)) {
                        throw new AppException(ErrorCode.BAD_REQUEST, "Mock endpoint with this method and path already exists");
                    }
                });

        if (request.getMethod() != null) {
            entity.setMethod(request.getMethod());
        }
        if (request.getPath() != null) {
            entity.setPath(MockEndpoint.normalizePath(request.getPath()));
        }
        if (request.getStatusCode() != null) {
            entity.setStatusCode(request.getStatusCode());
        }
        if (request.getDelayMs() != null) {
            entity.setDelayMs(request.getDelayMs());
        }
        if (request.getResponseHeadersJson() != null) {
            entity.setResponseHeadersJson(request.getResponseHeadersJson());
        }
        if (request.getResponseBody() != null) {
            entity.setResponseBody(request.getResponseBody());
        }
        if (request.getIsEnabled() != null) {
            entity.setEnabled(request.getIsEnabled());
        }

        entity = mockEndpointRepository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public void deleteMockEndpoint(UUID mockEndpointId) {
        MockEndpoint entity = mockEndpointRepository.findById(mockEndpointId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Mock endpoint not found"));

        validateProjectAccess(entity.getProjectId());

        mockEndpointRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public MockEndpoint handleRuntimeMock(UUID projectId, String methodStr, String subpath) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }

        HttpMethodType method;
        try {
            method = HttpMethodType.valueOf(methodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid HTTP Method");
        }

        String normalizedSubpath = MockEndpoint.normalizePath(subpath);

        // 1. Try exact match first
        MockEndpoint exactMatch = mockEndpointRepository
                .findByProjectIdAndMethodAndPath(projectId, method, normalizedSubpath)
                .orElse(null);

        if (exactMatch != null && exactMatch.isEnabled()) {
            simulateDelay(exactMatch);
            return exactMatch;
        }

        // 2. Pattern Matching via AntPathMatcher
        List<MockEndpoint> candidates = mockEndpointRepository
                .findByProjectIdAndMethodAndIsEnabledTrue(projectId, method);

        List<MockEndpoint> matches = new ArrayList<>();
        for (MockEndpoint candidate : candidates) {
            if (pathMatcher.match(candidate.getPath(), normalizedSubpath)) {
                matches.add(candidate);
            }
        }

        if (matches.isEmpty()) {
            throw new AppException(ErrorCode.NOT_FOUND, "No matching mock endpoint found");
        }

        // Sort match candidates to find the most specific pattern
        matches.sort((a, b) -> pathMatcher.getPatternComparator(normalizedSubpath).compare(a.getPath(), b.getPath()));
        MockEndpoint bestMatch = matches.get(0);

        simulateDelay(bestMatch);
        return bestMatch;
    }

    private void validateProjectAccess(UUID projectId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }
        projectRepository.findByIdAndUserAccess(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN_ACTION, "User does not have access to this project"));
    }

    private void simulateDelay(MockEndpoint mockEndpoint) {
        int delay = Math.min(mockEndpoint.getDelayMs(), 10000); // Limit delay to 10s maximum to avoid freezing threads
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
