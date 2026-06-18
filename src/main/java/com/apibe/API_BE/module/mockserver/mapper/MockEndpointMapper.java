package com.apibe.API_BE.module.mockserver.mapper;

import com.apibe.API_BE.module.mockserver.dto.request.CreateMockEndpointRequest;
import com.apibe.API_BE.module.mockserver.dto.response.MockEndpointResponse;
import com.apibe.API_BE.module.mockserver.entity.MockEndpoint;
import org.springframework.stereotype.Component;

@Component
public class MockEndpointMapper {

    public MockEndpointResponse toResponse(MockEndpoint entity) {
        if (entity == null) {
            return null;
        }
        return MockEndpointResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .method(entity.getMethod())
                .path(entity.getPath())
                .statusCode(entity.getStatusCode())
                .delayMs(entity.getDelayMs())
                .responseHeadersJson(entity.getResponseHeadersJson())
                .responseBody(entity.getResponseBody())
                .isEnabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MockEndpoint toEntity(CreateMockEndpointRequest request) {
        if (request == null) {
            return null;
        }
        return MockEndpoint.builder()
                .method(request.getMethod())
                .path(request.getPath())
                .statusCode(request.getStatusCode())
                .delayMs(request.getDelayMs())
                .responseHeadersJson(request.getResponseHeadersJson())
                .responseBody(request.getResponseBody())
                .build();
    }
}
