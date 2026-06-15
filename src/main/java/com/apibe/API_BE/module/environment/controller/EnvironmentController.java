package com.apibe.API_BE.module.environment.controller;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.environment.dto.request.*;
import com.apibe.API_BE.module.environment.dto.response.*;
import com.apibe.API_BE.module.environment.service.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;

    // T035: Lấy danh sách environments theo project
    @GetMapping("/projects/{projectId}/environments")
    public ResponseEntity<List<EnvironmentResponse>> getEnvironments(@PathVariable UUID projectId) {
        return ResponseEntity.ok(environmentService.getEnvironments(projectId));
    }

    // T036: Tạo environment mới theo project
    @PostMapping("/projects/{projectId}/environments")
    public ResponseEntity<EnvironmentResponse> createEnvironment(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateEnvironmentRequest request) {
        return ResponseEntity.ok(environmentService.createEnvironment(projectId, request));
    }

    // Cập nhật environment
    @PatchMapping("/environments/{environmentId}")
    public ResponseEntity<EnvironmentResponse> updateEnvironment(
            @PathVariable UUID environmentId,
            @Valid @RequestBody CreateEnvironmentRequest request) {
        return ResponseEntity.ok(environmentService.updateEnvironment(environmentId, request));
    }

    // Xóa environment
    @DeleteMapping("/environments/{environmentId}")
    public ResponseEntity<Void> deleteEnvironment(@PathVariable UUID environmentId) {
        environmentService.deleteEnvironment(environmentId);
        return ResponseEntity.noContent().build();
    }

    // T037: Chọn active environment
    @PutMapping("/projects/{projectId}/active-environment")
    public ResponseEntity<ActiveEnvironmentResponse> setActiveEnvironment(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID environmentId,
            @RequestBody(required = false) ActiveEnvironmentRequest request) {
        UUID envId = environmentId;
        if (envId == null && request != null) {
            envId = request.getEnvironmentId();
        }
        if (envId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        return ResponseEntity.ok(environmentService.setActiveEnvironment(projectId, envId));
    }

    // Lấy active environment hiện tại
    @GetMapping("/projects/{projectId}/active-environment")
    public ResponseEntity<EnvironmentResponse> getActiveEnvironment(@PathVariable UUID projectId) {
        return ResponseEntity.ok(environmentService.getActiveEnvironment(projectId));
    }

    // T038: Thêm biến môi trường mới
    @PostMapping("/environments/{environmentId}/variables")
    public ResponseEntity<EnvironmentVariableResponse> addVariable(
            @PathVariable UUID environmentId,
            @Valid @RequestBody CreateVariableRequest request) {
        return ResponseEntity.ok(environmentService.addVariable(environmentId, request));
    }

    // Cập nhật biến môi trường
    @PatchMapping("/environments/variables/{variableId}")
    public ResponseEntity<EnvironmentVariableResponse> updateVariable(
            @PathVariable UUID variableId,
            @RequestBody CreateVariableRequest request) {
        return ResponseEntity.ok(environmentService.updateVariable(variableId, request));
    }

    // Xóa biến môi trường
    @DeleteMapping("/environments/variables/{variableId}")
    public ResponseEntity<Void> deleteVariable(@PathVariable UUID variableId) {
        environmentService.deleteVariable(variableId);
        return ResponseEntity.noContent().build();
    }

    // T039: Resolve biến trong chuỗi văn bản
    @PostMapping("/projects/{projectId}/environments/resolve")
    public ResponseEntity<ResolveResponse> resolveVariables(
            @PathVariable UUID projectId,
            @RequestBody ResolveRequest request) {
        return ResponseEntity.ok(environmentService.resolveVariables(projectId, request));
    }
}
