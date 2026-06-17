package com.apibe.API_BE.module.collection.controller;

import com.apibe.API_BE.global.response.ApiResponse;
import com.apibe.API_BE.module.collection.dto.request.CreateApiRequestRequest;
import com.apibe.API_BE.module.collection.dto.request.UpdateApiRequestRequest;
import com.apibe.API_BE.module.collection.dto.response.ApiRequestResponse;
import com.apibe.API_BE.module.collection.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApiRequestController {

    private final CollectionService collectionService;

    // ── T032 ──────────────────────────────────────────────────────────────────
    // POST /api/collections/{collectionId}/requests
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/api/collections/{collectionId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ApiRequestResponse> createRequest(
            @PathVariable UUID collectionId,
            @Valid @RequestBody CreateApiRequestRequest request) {
        ApiRequestResponse data = collectionService.createRequest(collectionId, request);
        return ApiResponse.success("Request created successfully", data);
    }

    // ── T033 ──────────────────────────────────────────────────────────────────
    // PATCH /api/requests/{requestId}
    // ─────────────────────────────────────────────────────────────────────────

    @PatchMapping("/api/requests/{requestId}")
    public ApiResponse<ApiRequestResponse> updateRequest(
            @PathVariable UUID requestId,
            @RequestBody UpdateApiRequestRequest request) {
        ApiRequestResponse data = collectionService.updateRequest(requestId, request);
        return ApiResponse.success("Request updated successfully", data);
    }
}
