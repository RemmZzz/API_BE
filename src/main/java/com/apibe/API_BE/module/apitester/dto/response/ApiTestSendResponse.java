package com.apibe.API_BE.module.apitester.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestSendResponse {

    private UUID historyId;
    private UUID projectId;
    private String method;
    private String url;

    /** Raw JSON string of the request headers sent */
    private String requestHeaders;

    /** Raw JSON string of the query params sent */
    private String requestParams;

    /** Raw JSON string of the request body sent */
    private String requestBody;

    private Integer statusCode;
    private String statusText;

    /** Raw JSON string of the response headers received */
    private String responseHeaders;

    /** Response body as raw string */
    private String responseBody;

    private Long durationMs;

    /** true when the external server responded (even 4xx/5xx counts as success here) */
    private boolean success;

    /** Populated when a network/timeout/DNS error prevents any response */
    private String errorMessage;

    private LocalDateTime createdAt;
}
