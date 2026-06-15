package com.apibe.API_BE.module.documentation.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDocumentationResponse {
    private UUID projectId;
    private String title;
    private String description;
    private String version;
    private List<ApiDocumentationEndpointResponse> endpoints;
    private LocalDateTime updatedAt;
}
