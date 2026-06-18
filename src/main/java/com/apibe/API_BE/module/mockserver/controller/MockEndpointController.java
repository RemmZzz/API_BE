package com.apibe.API_BE.module.mockserver.controller;

import com.apibe.API_BE.global.response.ApiResponse;
import com.apibe.API_BE.module.mockserver.dto.request.CreateMockEndpointRequest;
import com.apibe.API_BE.module.mockserver.dto.request.UpdateMockEndpointRequest;
import com.apibe.API_BE.module.mockserver.dto.response.MockEndpointResponse;
import com.apibe.API_BE.module.mockserver.entity.MockEndpoint;
import com.apibe.API_BE.module.mockserver.service.MockEndpointService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MockEndpointController {

    private final MockEndpointService mockEndpointService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────────────────
    // T047 – GET /api/projects/{projectId}/mock-endpoints
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/api/projects/{projectId}/mock-endpoints")
    public ApiResponse<List<MockEndpointResponse>> getMockEndpoints(@PathVariable UUID projectId) {
        List<MockEndpointResponse> data = mockEndpointService.getMockEndpoints(projectId);
        return ApiResponse.success("Mock endpoints retrieved successfully", data);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T048 – POST /api/projects/{projectId}/mock-endpoints
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/api/projects/{projectId}/mock-endpoints")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MockEndpointResponse> createMockEndpoint(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateMockEndpointRequest request) {
        MockEndpointResponse data = mockEndpointService.createMockEndpoint(projectId, request);
        return ApiResponse.success("Mock endpoint created successfully", data);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T049 – PATCH /api/mock-endpoints/{mockEndpointId}
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/api/mock-endpoints/{mockEndpointId}")
    public ApiResponse<MockEndpointResponse> updateMockEndpoint(
            @PathVariable UUID mockEndpointId,
            @Valid @RequestBody UpdateMockEndpointRequest request) {
        MockEndpointResponse data = mockEndpointService.updateMockEndpoint(mockEndpointId, request);
        return ApiResponse.success("Mock endpoint updated successfully", data);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T049 – DELETE /api/mock-endpoints/{mockEndpointId}
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/api/mock-endpoints/{mockEndpointId}")
    public ApiResponse<Void> deleteMockEndpoint(@PathVariable UUID mockEndpointId) {
        mockEndpointService.deleteMockEndpoint(mockEndpointId);
        return ApiResponse.success("Mock endpoint deleted successfully", null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T050 – RequestMapping /mock/{projectId}/**
    // ─────────────────────────────────────────────────────────────────────────
    @RequestMapping(value = "/mock/{projectId}/**")
    public ResponseEntity<String> executeMock(
            @PathVariable UUID projectId,
            HttpServletRequest request) {
        String method = request.getMethod();

        String fullPath = request.getRequestURI();
        String prefix = "/mock/" + projectId;
        String subpath = "/";
        int index = fullPath.indexOf(prefix);
        if (index != -1) {
            subpath = fullPath.substring(index + prefix.length());
        }
        if (subpath.isEmpty()) {
            subpath = "/";
        }

        MockEndpoint mockEndpoint = mockEndpointService.handleRuntimeMock(projectId, method, subpath);

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(mockEndpoint.getStatusCode());

        if (mockEndpoint.getResponseHeadersJson() != null && !mockEndpoint.getResponseHeadersJson().isBlank()) {
            try {
                Map<String, String> headersMap = objectMapper.readValue(
                        mockEndpoint.getResponseHeadersJson(),
                        new TypeReference<Map<String, String>>() {}
                );
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    responseBuilder.header(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        return responseBuilder.body(mockEndpoint.getResponseBody());
    }
}
