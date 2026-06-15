package com.apibe.API_BE.module.database.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.database.dto.request.CreateColumnRequest;
import com.apibe.API_BE.module.database.dto.request.UpdateColumnRequest;
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
public class DatabaseColumnService {

    private final DatabaseColumnRepository databaseColumnRepository;
    private final DatabaseTableRepository databaseTableRepository;
    private final DatabaseSchemaRepository databaseSchemaRepository;
    private final DatabaseSchemaService databaseSchemaService;

    @Transactional
    public DatabaseSchemaResponse addColumn(UUID tableId, CreateColumnRequest request) {
        DatabaseTable table = databaseTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy bảng"));

        DatabaseSchema schema = databaseSchemaRepository.findById(table.getSchemaId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy schema"));

        List<DatabaseColumn> existingColumns = databaseColumnRepository.findByTableId(tableId);
        boolean exists = existingColumns.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()));
        if (exists) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Tên cột đã tồn tại trong bảng");
        }

        ColumnTypeInfo typeInfo = DatabaseTypeParser.parse(request.getType());
        int ordinal = existingColumns.size() + 1;

        DatabaseColumn column = DatabaseColumn.builder()
                .tableId(tableId)
                .name(request.getName())
                .dataType(typeInfo.getDataType())
                .length(typeInfo.getLength())
                .precisionValue(typeInfo.getPrecisionValue())
                .scaleValue(typeInfo.getScaleValue())
                .isPrimaryKey(request.isPrimaryKey())
                .isNullable(request.isNullable())
                .isUnique(request.isUnique())
                .isAutoIncrement(false)
                .defaultValue(request.getDefaultValue())
                .ordinalPosition(ordinal)
                .comment(request.getComment())
                .build();

        databaseColumnRepository.save(column);

        return databaseSchemaService.getSchemaByProjectId(schema.getProjectId());
    }

    @Transactional
    public DatabaseSchemaResponse updateColumn(UUID columnId, UpdateColumnRequest request) {
        DatabaseColumn column = databaseColumnRepository.findById(columnId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy cột"));

        DatabaseTable table = databaseTableRepository.findById(column.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy bảng"));

        DatabaseSchema schema = databaseSchemaRepository.findById(table.getSchemaId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy schema"));

        if (request.getName() != null) {
            List<DatabaseColumn> existingColumns = databaseColumnRepository.findByTableId(column.getTableId());
            boolean exists = existingColumns.stream()
                    .anyMatch(c -> !c.getId().equals(columnId) && c.getName().equalsIgnoreCase(request.getName()));
            if (exists) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Tên cột đã tồn tại trong bảng");
            }
            column.setName(request.getName());
        }

        if (request.getType() != null) {
            ColumnTypeInfo typeInfo = DatabaseTypeParser.parse(request.getType());
            column.setDataType(typeInfo.getDataType());
            column.setLength(typeInfo.getLength());
            column.setPrecisionValue(typeInfo.getPrecisionValue());
            column.setScaleValue(typeInfo.getScaleValue());
        }

        if (request.getPrimaryKey() != null) {
            column.setPrimaryKey(request.getPrimaryKey());
        }
        if (request.getNullable() != null) {
            column.setNullable(request.getNullable());
        }
        if (request.getUnique() != null) {
            column.setUnique(request.getUnique());
        }
        if (request.getDefaultValue() != null) {
            column.setDefaultValue(request.getDefaultValue());
        }
        if (request.getOrdinalPosition() != null) {
            column.setOrdinalPosition(request.getOrdinalPosition());
        }
        if (request.getComment() != null) {
            column.setComment(request.getComment());
        }

        databaseColumnRepository.save(column);

        return databaseSchemaService.getSchemaByProjectId(schema.getProjectId());
    }

    @Transactional
    public DatabaseSchemaResponse deleteColumn(UUID columnId) {
        DatabaseColumn column = databaseColumnRepository.findById(columnId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy cột"));

        DatabaseTable table = databaseTableRepository.findById(column.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy bảng"));

        DatabaseSchema schema = databaseSchemaRepository.findById(table.getSchemaId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Không tìm thấy schema"));

        databaseColumnRepository.delete(column);

        return databaseSchemaService.getSchemaByProjectId(schema.getProjectId());
    }
}
