package com.apibe.API_BE.module.documentation.controller;

import com.apibe.API_BE.module.documentation.dto.request.SaveApiDocumentationRequest;
import com.apibe.API_BE.module.documentation.dto.response.ApiDocumentationResponse;
import com.apibe.API_BE.module.documentation.service.ApiDocumentationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/documentation")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ApiDocumentationController {

    private final ApiDocumentationService apiDocumentationService;

    @GetMapping
    public ResponseEntity<ApiDocumentationResponse> getDocumentation(@PathVariable UUID projectId) {
        return ResponseEntity.ok(apiDocumentationService.getDocumentation(projectId));
    }

    @PutMapping
    public ResponseEntity<ApiDocumentationResponse> saveDocumentation(
            @PathVariable UUID projectId,
            @RequestBody SaveApiDocumentationRequest request) {
        return ResponseEntity.ok(apiDocumentationService.saveDocumentation(projectId, request));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiDocumentationResponse> generateDocumentation(@PathVariable UUID projectId) {
        return ResponseEntity.ok(apiDocumentationService.generateDocumentationFromCollections(projectId));
    }

    @GetMapping(value = "/export.md", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportMarkdown(@PathVariable UUID projectId) {
        return ResponseEntity.ok(apiDocumentationService.exportMarkdown(projectId));
    }
}
