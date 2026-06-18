package com.apibe.API_BE.module.mockserver.dto.response;

import com.apibe.API_BE.global.enums.HttpMethodType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockEndpointResponse {
    private UUID id;
    private UUID projectId;
    private HttpMethodType method;
    private String path;
    private int statusCode;
    private int delayMs;
    private String responseHeadersJson;
    private String responseBody;
    private boolean isEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
