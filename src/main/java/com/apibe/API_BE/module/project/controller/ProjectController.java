package com.apibe.API_BE.module.project.controller;

import com.apibe.API_BE.global.response.PageResponse;
import com.apibe.API_BE.module.project.dto.request.CreateProjectRequest;
import com.apibe.API_BE.module.project.dto.request.UpdateProjectRequest;
import com.apibe.API_BE.module.project.dto.response.ProjectDetailResponse;
import com.apibe.API_BE.module.project.dto.response.ProjectResponse;
import com.apibe.API_BE.module.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<PageResponse<ProjectResponse>> getProjects(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(projectService.getProjects(keyword, status, type, page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ProjectResponse> duplicateProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.duplicateProject(id));
    }
}
