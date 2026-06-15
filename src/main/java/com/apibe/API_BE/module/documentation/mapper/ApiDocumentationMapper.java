package com.apibe.API_BE.module.documentation.mapper;

import com.apibe.API_BE.module.documentation.dto.response.*;
import com.apibe.API_BE.module.documentation.entity.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApiDocumentationMapper {

    private final ObjectMapper objectMapper;

    public ApiDocumentationResponse toResponse(ApiDocumentation doc) {
        if (doc == null) {
            return null;
        }

        List<ApiDocumentationEndpointResponse> endpointResponses = Collections.emptyList();
        if (doc.getEndpoints() != null) {
            endpointResponses = doc.getEndpoints().stream()
                    .map(this::toEndpointResponse)
                    .collect(Collectors.toList());
        }

        return ApiDocumentationResponse.builder()
                .projectId(doc.getProjectId())
                .title(doc.getTitle())
                .version(doc.getVersion())
                .endpoints(endpointResponses)
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    public ApiDocumentationEndpointResponse toEndpointResponse(ApiDocumentationEndpoint endpoint) {
        if (endpoint == null) {
            return null;
        }

        Object headers = parseJson(endpoint.getHeadersJson());
        Object params = parseJson(endpoint.getParamsJson());

        return ApiDocumentationEndpointResponse.builder()
                .id(endpoint.getId())
                .method(endpoint.getMethod())
                .url(endpoint.getUrl())
                .description(endpoint.getDescription())
                .headers(headers)
                .params(params)
                .bodyExample(endpoint.getBodyExample())
                .responseExample(endpoint.getResponseExample())
                .errorExample(endpoint.getErrorExample())
                .createdAt(endpoint.getCreatedAt())
                .updatedAt(endpoint.getUpdatedAt())
                .build();
    }

    private Object parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
