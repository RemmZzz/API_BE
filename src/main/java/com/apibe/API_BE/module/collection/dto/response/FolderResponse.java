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
public class FolderResponse {

    private UUID id;
    private UUID collectionId;
    private UUID parentFolderId;
    private String name;
    private Integer sortOrder;

    /** Nested child folders */
    private List<FolderResponse> children;

    /** Requests directly inside this folder */
    private List<ApiRequestResponse> requests;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
