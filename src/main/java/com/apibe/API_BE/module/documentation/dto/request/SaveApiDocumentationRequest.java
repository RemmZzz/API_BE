package com.apibe.API_BE.module.documentation.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveApiDocumentationRequest {
    private String title;
    private String version;
    private List<SaveApiDocumentationEndpointRequest> endpoints;
}
