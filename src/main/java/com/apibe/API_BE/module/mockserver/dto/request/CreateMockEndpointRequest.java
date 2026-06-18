package com.apibe.API_BE.module.mockserver.dto.request;

import com.apibe.API_BE.global.enums.HttpMethodType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMockEndpointRequest {

    @NotNull(message = "HTTP Method is required")
    private HttpMethodType method;

    @NotBlank(message = "Path is required")
    private String path;

    @Builder.Default
    private int statusCode = 200;

    @Min(value = 0, message = "Delay must be non-negative")
    private int delayMs;

    private String responseHeadersJson;

    private String responseBody;
}
