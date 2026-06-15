package com.apibe.API_BE.module.database.controller;

import com.apibe.API_BE.module.database.dto.request.UpdateColumnRequest;
import com.apibe.API_BE.module.database.dto.response.DatabaseSchemaResponse;
import com.apibe.API_BE.module.database.service.DatabaseColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/database/columns")
@RequiredArgsConstructor
public class DatabaseColumnController {

    private final DatabaseColumnService databaseColumnService;

    @PatchMapping("/{columnId}")
    public ResponseEntity<DatabaseSchemaResponse> updateColumn(
            @PathVariable UUID columnId,
            @RequestBody UpdateColumnRequest request) {
        return ResponseEntity.ok(databaseColumnService.updateColumn(columnId, request));
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<DatabaseSchemaResponse> deleteColumn(@PathVariable UUID columnId) {
        return ResponseEntity.ok(databaseColumnService.deleteColumn(columnId));
    }
}
