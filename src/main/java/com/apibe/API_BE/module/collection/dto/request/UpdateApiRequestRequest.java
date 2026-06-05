package com.apibe.API_BE.module.collection.dto.request;

import com.apibe.API_BE.global.enums.HttpMethodType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApiRequestRequest {

    private String name;

    private UUID folderId;

    private HttpMethodType method;

    private String url;

    /** Raw JSON string for headers map */
    private String headers;

    /** Raw JSON string for query params map */
    private String params;

    /** Raw JSON string for request body */
    private String body;

    private String description;
}
