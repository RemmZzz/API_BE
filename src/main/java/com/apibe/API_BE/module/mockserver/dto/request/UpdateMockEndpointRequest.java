package com.apibe.API_BE.module.mockserver.dto.request;

import com.apibe.API_BE.global.enums.HttpMethodType;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMockEndpointRequest {

    private HttpMethodType method;

    private String path;

    private Integer statusCode;

    @Min(value = 0, message = "Delay must be non-negative")
    private Integer delayMs;

    private String responseHeadersJson;

    private String responseBody;

    private Boolean isEnabled;
}
