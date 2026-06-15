package com.apibe.API_BE.module.database.controller;

import com.apibe.API_BE.module.database.dto.request.CreateColumnRequest;
import com.apibe.API_BE.module.database.dto.request.UpdateTableRequest;
import com.apibe.API_BE.module.database.dto.response.DatabaseSchemaResponse;
import com.apibe.API_BE.module.database.service.DatabaseColumnService;
import com.apibe.API_BE.module.database.service.DatabaseTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/database/tables")
@RequiredArgsConstructor
public class DatabaseTableController {

    private final DatabaseTableService databaseTableService;
    private final DatabaseColumnService databaseColumnService;

    @PatchMapping("/{tableId}")
    public ResponseEntity<DatabaseSchemaResponse> updateTable(
            @PathVariable UUID tableId,
            @RequestBody UpdateTableRequest request) {
        return ResponseEntity.ok(databaseTableService.updateTable(tableId, request));
    }

    @DeleteMapping("/{tableId}")
    public ResponseEntity<DatabaseSchemaResponse> deleteTable(@PathVariable UUID tableId) {
        return ResponseEntity.ok(databaseTableService.deleteTable(tableId));
    }

    @PostMapping("/{tableId}/columns")
    public ResponseEntity<DatabaseSchemaResponse> addColumn(
            @PathVariable UUID tableId,
            @RequestBody CreateColumnRequest request) {
        return ResponseEntity.ok(databaseColumnService.addColumn(tableId, request));
    }
}
