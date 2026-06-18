package com.apibe.API_BE.module.collection.controller;

import com.apibe.API_BE.global.response.ApiResponse;
import com.apibe.API_BE.module.collection.dto.request.CreateCollectionRequest;
import com.apibe.API_BE.module.collection.dto.request.CreateFolderRequest;
import com.apibe.API_BE.module.collection.dto.response.CollectionResponse;
import com.apibe.API_BE.module.collection.dto.response.FolderResponse;
import com.apibe.API_BE.module.collection.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    // ── T029 ──────────────────────────────────────────────────────────────────
    // GET /api/projects/{projectId}/collections
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/projects/{projectId}/collections")
    public ApiResponse<List<CollectionResponse>> getCollections(
            @PathVariable UUID projectId) {
        List<CollectionResponse> data = collectionService.getCollectionsByProject(projectId);
        return ApiResponse.success("Collections retrieved successfully", data);
    }

    // ── T030 ──────────────────────────────────────────────────────────────────
    // POST /api/projects/{projectId}/collections
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/api/projects/{projectId}/collections")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CollectionResponse> createCollection(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateCollectionRequest request) {
        CollectionResponse data = collectionService.createCollection(projectId, request);
        return ApiResponse.success("Collection created successfully", data);
    }

    // ── T031 ──────────────────────────────────────────────────────────────────
    // POST /api/collections/{collectionId}/folders
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/api/collections/{collectionId}/folders")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FolderResponse> createFolder(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CreateFolderRequest request) {
        FolderResponse data = collectionService.createFolder(collectionId, request);
        return ApiResponse.success("Folder created successfully", data);
    }
}
