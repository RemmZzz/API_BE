package com.apibe.API_BE.module.apitester.dto.request;

import com.apibe.API_BE.global.enums.HttpMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTestSendRequest {

    @NotNull(message = "projectId is required")
    private UUID projectId;

    /** Optional display name for this test */
    private String name;

    @NotNull(message = "method is required")
    private HttpMethodType method;

    @NotBlank(message = "url is required")
    private String url;

    /** Optional request headers map */
    private Map<String, String> headers;

    /** Optional query params map */
    private Map<String, String> params;

    /** Optional request body (any JSON-serialisable value) */
    private Object body;

    /** Timeout in milliseconds; max 60 000 ms */
    @Builder.Default
    private int timeoutMs = 30_000;

    /** Whether to persist this request as a history record */
    @Builder.Default
    private boolean saveHistory = true;
}
