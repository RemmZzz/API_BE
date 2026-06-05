package com.apibe.API_BE.module.apitester.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestHistoryResponse {

    private UUID id;
    private UUID projectId;
    private String name;
    private String method;
    private String url;
    private String requestHeaders;
    private String requestParams;
    private String requestBody;
    private Integer statusCode;
    private String statusText;
    private String responseHeaders;
    private String responseBody;
    private Long durationMs;
    private boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
