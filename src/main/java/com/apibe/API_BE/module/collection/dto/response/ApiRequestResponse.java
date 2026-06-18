package com.apibe.API_BE.module.collection.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRequestResponse {

    private UUID id;
    private UUID collectionId;
    private UUID folderId;
    private String name;
    private String method;
    private String url;

    /** Raw JSON string */
    private String headers;

    /** Raw JSON string */
    private String params;

    /** Raw JSON string */
    private String body;

    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
