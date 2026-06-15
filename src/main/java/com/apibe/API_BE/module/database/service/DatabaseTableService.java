package com.apibe.API_BE.module.database.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.database.dto.request.CreateColumnRequest;
import com.apibe.API_BE.module.database.dto.request.CreateTableRequest;
import com.apibe.API_BE.module.database.dto.request.UpdateTableRequest;
import com.apibe.API_BE.module.database.dto.response.DatabaseSchemaResponse;
import com.apibe.API_BE.module.database.entity.DatabaseColumn;
import com.apibe.API_BE.module.database.entity.DatabaseSchema;
import com.apibe.API_BE.module.database.entity.DatabaseTable;
import com.apibe.API_BE.module.database.repository.DatabaseColumnRepository;
import com.apibe.API_BE.module.database.repository.DatabaseSchemaRepository;
import com.apibe.API_BE.module.database.repository.DatabaseTableRepository;
import com.apibe.API_BE.module.database.util.ColumnTypeInfo;
import com.apibe.API_BE.module.database.util.DatabaseTypeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DatabaseTableService {

    private final DatabaseTableRepository databaseTableRepository;
    private final DatabaseColumnRepository databaseColumnRepository;
    private final DatabaseSchemaRepository databaseSchemaRepository;
    private final DatabaseSchemaService databaseSchemaService;

    @Transactional
    public DatabaseSchemaResponse createTable(UUID projectId, CreateTableRequest request) {
        DatabaseSchema schema = databaseSchemaRepository.findByProjectId(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy schema cho dự án"));

        // Check duplicate name
        List<DatabaseTable> existingTables = databaseTableRepository.findBySchemaId(schema.getId());
        boolean exists = existingTables.stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(request.getName()));
        if (exists) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tên bảng đã tồn tại");
        }

        DatabaseTable table = DatabaseTable.builder()
                .schemaId(schema.getId())
                .name(request.getName())
                .displayName(request.getName())
                .rowCount(0)
                .positionX(request.getPositionX() != null ? request.getPositionX() : 0)
                .positionY(request.getPositionY() != null ? request.getPositionY() : 0)
                .build();

        table = databaseTableRepository.save(table);

        if (request.getColumns() != null) {
            int ordinal = 1;
            for (CreateColumnRequest colReq : request.getColumns()) {
                ColumnTypeInfo typeInfo = DatabaseTypeParser.parse(colReq.getType());
                DatabaseColumn col = DatabaseColumn.builder()
                        .tableId(table.getId())
                        .name(colReq.getName())
                        .dataType(typeInfo.getDataType())
                        .length(typeInfo.getLength())
                        .precisionValue(typeInfo.getPrecisionValue())
                        .scaleValue(typeInfo.getScaleValue())
                        .isPrimaryKey(colReq.isPrimaryKey())
                        .isNullable(colReq.isNullable())
                        .isUnique(colReq.isUnique())
                        .isAutoIncrement(false)
                        .defaultValue(colReq.getDefaultValue())
                        .ordinalPosition(ordinal++)
                        .comment(colReq.getComment())
                        .build();
                databaseColumnRepository.save(col);
            }
        }

        return databaseSchemaService.getSchemaByProjectId(projectId);
    }

    @Transactional
    public DatabaseSchemaResponse updateTable(UUID tableId, UpdateTableRequest request) {
        DatabaseTable table = databaseTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy bảng"));

        DatabaseSchema schema = databaseSchemaRepository.findById(table.getSchemaId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy schema"));

        if (request.getName() != null) {
            // Check duplicate name
            List<DatabaseTable> existingTables = databaseTableRepository.findBySchemaId(schema.getId());
            boolean exists = existingTables.stream()
                    .anyMatch(t -> !t.getId().equals(tableId) && t.getName().equalsIgnoreCase(request.getName()));
            if (exists) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Tên bảng đã tồn tại");
            }
            table.setName(request.getName());
            table.setDisplayName(request.getName());
        }

        if (request.getPositionX() != null) {
            table.setPositionX(request.getPositionX());
        }
        if (request.getPositionY() != null) {
            table.setPositionY(request.getPositionY());
        }
        if (request.getRowCount() != null) {
            table.setRowCount(request.getRowCount());
        }

        databaseTableRepository.save(table);

        return databaseSchemaService.getSchemaByProjectId(schema.getProjectId());
    }

    @Transactional
    public DatabaseSchemaResponse deleteTable(UUID tableId) {
        DatabaseTable table = databaseTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy bảng"));

        DatabaseSchema schema = databaseSchemaRepository.findById(table.getSchemaId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy schema"));

        // Delete related columns
        List<DatabaseColumn> columns = databaseColumnRepository.findByTableId(tableId);
        databaseColumnRepository.deleteAll(columns);

        databaseTableRepository.delete(table);

        return databaseSchemaService.getSchemaByProjectId(schema.getProjectId());
    }
}
