package com.apibe.API_BE.module.collection.dto.request;

import com.apibe.API_BE.global.enums.HttpMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApiRequestRequest {

    @NotBlank(message = "Request name is required")
    private String name;

    private UUID folderId;

    @NotNull(message = "HTTP method is required")
    private HttpMethodType method;

    @NotBlank(message = "URL is required")
    private String url;

    /** Raw JSON string for headers map */
    private String headers;

    /** Raw JSON string for query params map */
    private String params;

    /** Raw JSON string for request body */
    private String body;

    private String description;
}
