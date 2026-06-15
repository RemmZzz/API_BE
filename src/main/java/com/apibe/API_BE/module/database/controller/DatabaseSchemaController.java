package com.apibe.API_BE.module.database.controller;

import com.apibe.API_BE.module.database.dto.request.CreateRelationshipRequest;
import com.apibe.API_BE.module.database.dto.request.CreateTableRequest;
import com.apibe.API_BE.module.database.dto.request.SaveDatabaseSchemaRequest;
import com.apibe.API_BE.module.database.dto.response.DatabaseRelationshipResponse;
import com.apibe.API_BE.module.database.dto.response.DatabaseSchemaResponse;
import com.apibe.API_BE.module.database.dto.response.SqlPreviewResponse;
import com.apibe.API_BE.module.database.service.DatabaseSchemaService;
import com.apibe.API_BE.module.database.service.DatabaseTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DatabaseSchemaController {

    private final DatabaseSchemaService databaseSchemaService;
    private final DatabaseTableService databaseTableService;

    @GetMapping("/projects/{projectId}/database-schema")
    public ResponseEntity<DatabaseSchemaResponse> getSchema(@PathVariable UUID projectId) {
        return ResponseEntity.ok(databaseSchemaService.getSchemaByProjectId(projectId));
    }

    @PutMapping("/projects/{projectId}/database-schema")
    public ResponseEntity<DatabaseSchemaResponse> saveSchema(
            @PathVariable UUID projectId,
            @RequestBody SaveDatabaseSchemaRequest request) {
        return ResponseEntity.ok(databaseSchemaService.saveSchema(projectId, request));
    }

    @PostMapping("/projects/{projectId}/database-schema/tables")
    public ResponseEntity<DatabaseSchemaResponse> createTable(
            @PathVariable UUID projectId,
            @RequestBody CreateTableRequest request) {
        return ResponseEntity.ok(databaseTableService.createTable(projectId, request));
    }

    @PostMapping("/projects/{projectId}/database-schema/relationship")
    public ResponseEntity<DatabaseRelationshipResponse> createRelationship(
            @PathVariable UUID projectId,
            @RequestBody CreateRelationshipRequest request) {
        return ResponseEntity.ok(databaseSchemaService.createRelationship(projectId, request));
    }

    @GetMapping("/projects/{projectId}/database-schema/sql")
    public ResponseEntity<SqlPreviewResponse> getSqlPreview(@PathVariable UUID projectId) {
        return ResponseEntity.ok(databaseSchemaService.generateSqlPreview(projectId));
    }
}
