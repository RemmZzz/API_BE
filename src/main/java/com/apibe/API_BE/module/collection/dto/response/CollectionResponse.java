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
public class CollectionResponse {

    private UUID id;
    private UUID projectId;
    private String name;
    private String description;
    private Integer sortOrder;

    /** Nested folder tree (root folders with children) */
    private List<FolderResponse> folders;

    /** Requests at collection root level (no folder) */
    private List<ApiRequestResponse> requests;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
