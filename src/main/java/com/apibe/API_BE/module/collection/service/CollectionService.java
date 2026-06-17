package com.apibe.API_BE.module.collection.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.collection.dto.request.CreateApiRequestRequest;
import com.apibe.API_BE.module.collection.dto.request.CreateCollectionRequest;
import com.apibe.API_BE.module.collection.dto.request.CreateFolderRequest;
import com.apibe.API_BE.module.collection.dto.request.UpdateApiRequestRequest;
import com.apibe.API_BE.module.collection.dto.response.ApiRequestResponse;
import com.apibe.API_BE.module.collection.dto.response.CollectionResponse;
import com.apibe.API_BE.module.collection.dto.response.FolderResponse;
import com.apibe.API_BE.module.collection.entity.ApiRequest;
import com.apibe.API_BE.module.collection.entity.Collection;
import com.apibe.API_BE.module.collection.entity.CollectionFolder;
import com.apibe.API_BE.module.collection.mapper.CollectionMapper;
import com.apibe.API_BE.module.collection.repository.ApiRequestRepository;
import com.apibe.API_BE.module.collection.repository.CollectionFolderRepository;
import com.apibe.API_BE.module.collection.repository.CollectionRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final ProjectRepository projectRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionFolderRepository folderRepository;
    private final ApiRequestRepository requestRepository;
    private final CollectionMapper mapper;

    // ─────────────────────────────────────────────────────────────────────────
    // T029 – GET /api/projects/{projectId}/collections
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CollectionResponse> getCollectionsByProject(UUID projectId) {
        assertProjectExists(projectId);

        List<Collection> collections =
                collectionRepository.findByProjectIdOrderBySortOrderAscCreatedAtAsc(projectId);

        return collections.stream()
                .map(this::buildCollectionResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T030 – POST /api/projects/{projectId}/collections
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public CollectionResponse createCollection(UUID projectId, CreateCollectionRequest req) {
        assertProjectExists(projectId);

        Collection collection = Collection.builder()
                .projectId(projectId)
                .name(req.getName().trim())
                .description(req.getDescription())
                .build();

        collection = collectionRepository.save(collection);
        return buildCollectionResponse(collection);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T031 – POST /api/collections/{collectionId}/folders
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public FolderResponse createFolder(UUID collectionId, CreateFolderRequest req) {
        assertCollectionExists(collectionId);

        // Validate parent folder if provided
        if (req.getParentFolderId() != null) {
            folderRepository.findByIdAndCollectionId(req.getParentFolderId(), collectionId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,
                            "Parent folder not found in this collection"));
        }

        CollectionFolder folder = CollectionFolder.builder()
                .collectionId(collectionId)
                .parentFolderId(req.getParentFolderId())
                .name(req.getName().trim())
                .build();

        folder = folderRepository.save(folder);

        // Newly created folder has no children or requests yet
        return mapper.toFolderResponse(folder, List.of(), List.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T032 – POST /api/collections/{collectionId}/requests
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ApiRequestResponse createRequest(UUID collectionId, CreateApiRequestRequest req) {
        assertCollectionExists(collectionId);

        // Validate folderId if provided
        if (req.getFolderId() != null) {
            folderRepository.findByIdAndCollectionId(req.getFolderId(), collectionId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,
                            "Folder not found in this collection"));
        }

        ApiRequest apiRequest = ApiRequest.builder()
                .collectionId(collectionId)
                .folderId(req.getFolderId())
                .name(req.getName().trim())
                .method(req.getMethod())
                .url(req.getUrl().trim())
                .headers(req.getHeaders())
                .params(req.getParams())
                .body(req.getBody())
                .description(req.getDescription())
                .build();

        apiRequest = requestRepository.save(apiRequest);
        return mapper.toApiRequestResponse(apiRequest);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // T033 – PATCH /api/requests/{requestId}
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ApiRequestResponse updateRequest(UUID requestId, UpdateApiRequestRequest req) {
        ApiRequest apiRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Request not found"));

        // If changing folderId, validate it belongs to the same collection
        if (req.getFolderId() != null) {
            folderRepository.findByIdAndCollectionId(req.getFolderId(), apiRequest.getCollectionId())
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST,
                            "Folder does not belong to the same collection"));
        }

        // Partial update — only apply non-null fields
        if (req.getName() != null && !req.getName().isBlank()) {
            apiRequest.setName(req.getName().trim());
        }
        if (req.getFolderId() != null) {
            apiRequest.setFolderId(req.getFolderId());
        }
        if (req.getMethod() != null) {
            apiRequest.setMethod(req.getMethod());
        }
        if (req.getUrl() != null && !req.getUrl().isBlank()) {
            apiRequest.setUrl(req.getUrl().trim());
        }
        if (req.getHeaders() != null) {
            apiRequest.setHeaders(req.getHeaders());
        }
        if (req.getParams() != null) {
            apiRequest.setParams(req.getParams());
        }
        if (req.getBody() != null) {
            apiRequest.setBody(req.getBody());
        }
        if (req.getDescription() != null) {
            apiRequest.setDescription(req.getDescription());
        }

        apiRequest = requestRepository.save(apiRequest);
        return mapper.toApiRequestResponse(apiRequest);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void assertProjectExists(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }
    }

    private void assertCollectionExists(UUID collectionId) {
        if (!collectionRepository.existsById(collectionId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Collection not found");
        }
    }

    /**
     * Builds a full CollectionResponse with nested folder tree and root-level requests.
     */
    private CollectionResponse buildCollectionResponse(Collection collection) {
        UUID collectionId = collection.getId();

        // All folders in this collection
        List<CollectionFolder> allFolders =
                folderRepository.findByCollectionIdOrderBySortOrderAscCreatedAtAsc(collectionId);

        // All requests in this collection
        List<ApiRequest> allRequests =
                requestRepository.findByCollectionIdOrderBySortOrderAscCreatedAtAsc(collectionId);

        // Group requests by folderId (null = root)
        Map<UUID, List<ApiRequestResponse>> requestsByFolder = allRequests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getFolderId() != null ? r.getFolderId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        Collectors.mapping(mapper::toApiRequestResponse, Collectors.toList())
                ));

        UUID rootKey = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // Build folder tree
        Map<UUID, CollectionFolder> folderMap = allFolders.stream()
                .collect(Collectors.toMap(CollectionFolder::getId, f -> f));
        Map<UUID, List<CollectionFolder>> childrenMap = allFolders.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getParentFolderId() != null
                                ? f.getParentFolderId()
                                : UUID.fromString("00000000-0000-0000-0000-000000000000")
                ));

        // Build root folder responses
        List<FolderResponse> rootFolderResponses = allFolders.stream()
                .filter(f -> f.getParentFolderId() == null)
                .map(f -> buildFolderResponse(f, childrenMap, requestsByFolder))
                .collect(Collectors.toList());

        List<ApiRequestResponse> rootRequests =
                requestsByFolder.getOrDefault(rootKey, List.of());

        return mapper.toCollectionResponse(collection, rootFolderResponses, rootRequests);
    }

    /**
     * Recursively builds FolderResponse with children and requests.
     */
    private FolderResponse buildFolderResponse(
            CollectionFolder folder,
            Map<UUID, List<CollectionFolder>> childrenMap,
            Map<UUID, List<ApiRequestResponse>> requestsByFolder) {

        List<CollectionFolder> children =
                childrenMap.getOrDefault(folder.getId(), List.of());

        List<FolderResponse> childResponses = children.stream()
                .map(c -> buildFolderResponse(c, childrenMap, requestsByFolder))
                .collect(Collectors.toList());

        List<ApiRequestResponse> folderRequests =
                requestsByFolder.getOrDefault(folder.getId(), List.of());

        return mapper.toFolderResponse(folder, childResponses, folderRequests);
    }
}
