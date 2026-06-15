package com.apibe.API_BE.module.documentation.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDocumentationEndpointResponse {
    private UUID id;
    private String method;
    private String url;
    private String description;
    private Object headers;
    private Object params;
    private String bodyExample;
    private String responseExample;
    private String errorExample;
    private int ordinalPosition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
