package com.apibe.API_BE.module.collection.mapper;

import com.apibe.API_BE.module.collection.dto.response.ApiRequestResponse;
import com.apibe.API_BE.module.collection.dto.response.CollectionResponse;
import com.apibe.API_BE.module.collection.dto.response.FolderResponse;
import com.apibe.API_BE.module.collection.entity.ApiRequest;
import com.apibe.API_BE.module.collection.entity.Collection;
import com.apibe.API_BE.module.collection.entity.CollectionFolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectionMapper {

    public ApiRequestResponse toApiRequestResponse(ApiRequest req) {
        if (req == null) return null;
        return ApiRequestResponse.builder()
                .id(req.getId())
                .collectionId(req.getCollectionId())
                .folderId(req.getFolderId())
                .name(req.getName())
                .method(req.getMethod() != null ? req.getMethod().name() : null)
                .url(req.getUrl())
                .headers(req.getHeaders())
                .params(req.getParams())
                .body(req.getBody())
                .description(req.getDescription())
                .sortOrder(req.getSortOrder())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }

    public FolderResponse toFolderResponse(
            CollectionFolder folder,
            List<FolderResponse> children,
            List<ApiRequestResponse> requests) {
        if (folder == null) return null;
        return FolderResponse.builder()
                .id(folder.getId())
                .collectionId(folder.getCollectionId())
                .parentFolderId(folder.getParentFolderId())
                .name(folder.getName())
                .sortOrder(folder.getSortOrder())
                .children(children)
                .requests(requests)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }

    public CollectionResponse toCollectionResponse(
            Collection collection,
            List<FolderResponse> folders,
            List<ApiRequestResponse> rootRequests) {
        if (collection == null) return null;
        return CollectionResponse.builder()
                .id(collection.getId())
                .projectId(collection.getProjectId())
                .name(collection.getName())
                .description(collection.getDescription())
                .sortOrder(collection.getSortOrder())
                .folders(folders)
                .requests(rootRequests)
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .build();
    }
}
