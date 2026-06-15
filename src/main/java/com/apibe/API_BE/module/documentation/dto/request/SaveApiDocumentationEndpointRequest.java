package com.apibe.API_BE.module.documentation.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveApiDocumentationEndpointRequest {
    private String id;
    private String method;
    private String url;
    private String description;
    private Object headers;
    private Object params;
    private String bodyExample;
    private String responseExample;
    private String errorExample;
}
