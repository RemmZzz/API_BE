package com.apibe.API_BE.module.database.service;

import com.apibe.API_BE.module.database.dto.request.CreateColumnRequest;
import com.apibe.API_BE.module.database.dto.request.CreateRelationshipRequest;
import com.apibe.API_BE.module.database.dto.request.CreateTableRequest;
import com.apibe.API_BE.module.database.dto.request.SaveDatabaseSchemaRequest;
import com.apibe.API_BE.module.database.dto.response.*;
import com.apibe.API_BE.module.database.entity.*;
import com.apibe.API_BE.module.database.mapper.DatabaseMapper;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.database.util.ColumnTypeInfo;
import com.apibe.API_BE.module.database.util.DatabaseTypeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DatabaseSchemaService {

    private final DatabaseSchemaRepository databaseSchemaRepository;
    private final DatabaseTableRepository databaseTableRepository;
    private final DatabaseColumnRepository databaseColumnRepository;
    private final DatabaseRelationshipRepository databaseRelationshipRepository;
    private final DatabaseMapper databaseMapper;

    public DatabaseSchemaResponse getSchemaByProjectId(UUID projectId) {
        DatabaseSchema schema = getOrCreateDefaultSchema(projectId);

        List<DatabaseTable> tables = databaseTableRepository.findBySchemaId(schema.getId());
        List<DatabaseTableResponse> tableResponses = tables.stream()
                .map(table -> {
                    List<DatabaseColumn> columns = databaseColumnRepository.findByTableId(table.getId());
                    return databaseMapper.toTableResponse(table, columns);
                })
                .collect(Collectors.toList());

        List<DatabaseRelationship> relationships = databaseRelationshipRepository.findBySchemaId(schema.getId());

        return databaseMapper.toSchemaResponse(schema, tableResponses, relationships);
    }

    @Transactional
    public DatabaseSchemaResponse saveSchema(UUID projectId, SaveDatabaseSchemaRequest request) {
        DatabaseSchema schema = getOrCreateDefaultSchema(projectId);

        if (request.getDbType() != null) {
            schema.setDbType(request.getDbType());
        }
        if (request.getName() != null) {
            schema.setName(request.getName());
        }
        schema = databaseSchemaRepository.save(schema);

        List<DatabaseTable> existingTables = databaseTableRepository.findBySchemaId(schema.getId());
        Map<String, DatabaseTable> existingTableMap = existingTables.stream()
                .collect(Collectors.toMap(t -> t.getName().toLowerCase(), t -> t, (t1, t2) -> t1));

        Set<UUID> keptTableIds = new HashSet<>();
        List<DatabaseTableResponse> savedTables = new ArrayList<>();

        if (request.getTables() != null) {
            for (CreateTableRequest tableReq : request.getTables()) {
                String tableNameLower = tableReq.getName().toLowerCase();
                DatabaseTable table;

                if (existingTableMap.containsKey(tableNameLower)) {
                    table = existingTableMap.get(tableNameLower);
                    if (tableReq.getPositionX() != null) table.setPositionX(tableReq.getPositionX());
                    if (tableReq.getPositionY() != null) table.setPositionY(tableReq.getPositionY());
                } else {
                    table = DatabaseTable.builder()
                            .schemaId(schema.getId())
                            .name(tableReq.getName())
                            .displayName(tableReq.getName())
                            .rowCount(0)
                            .positionX(tableReq.getPositionX() != null ? tableReq.getPositionX() : 0)
                            .positionY(tableReq.getPositionY() != null ? tableReq.getPositionY() : 0)
                            .build();
                }
                table = databaseTableRepository.save(table);
                keptTableIds.add(table.getId());

                List<DatabaseColumn> existingColumns = databaseColumnRepository.findByTableId(table.getId());
                Map<String, DatabaseColumn> existingColMap = existingColumns.stream()
                        .collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c, (c1, c2) -> c1));

                Set<UUID> keptColIds = new HashSet<>();
                List<DatabaseColumn> savedColumns = new ArrayList<>();

                if (tableReq.getColumns() != null) {
                    int ordinal = 1;
                    for (CreateColumnRequest colReq : tableReq.getColumns()) {
                        String colNameLower = colReq.getName().toLowerCase();
                        DatabaseColumn column;

                        ColumnTypeInfo typeInfo = DatabaseTypeParser.parse(colReq.getType());

                        if (existingColMap.containsKey(colNameLower)) {
                            column = existingColMap.get(colNameLower);
                            column.setDataType(typeInfo.getDataType());
                            column.setLength(typeInfo.getLength());
                            column.setPrecisionValue(typeInfo.getPrecisionValue());
                            column.setScaleValue(typeInfo.getScaleValue());
                            column.setPrimaryKey(colReq.isPrimaryKey());
                            column.setNullable(colReq.isNullable());
                            column.setUnique(colReq.isUnique());
                            column.setDefaultValue(colReq.getDefaultValue());
                            column.setOrdinalPosition(ordinal++);
                        } else {
                            column = DatabaseColumn.builder()
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
                                    .build();
                        }
                        column = databaseColumnRepository.save(column);
                        keptColIds.add(column.getId());
                        savedColumns.add(column);
                    }
                }

                // Delete columns not kept
                for (DatabaseColumn col : existingColumns) {
                    if (!keptColIds.contains(col.getId())) {
                        databaseColumnRepository.delete(col);
                    }
                }

                savedTables.add(databaseMapper.toTableResponse(table, savedColumns));
            }
        }

        // Delete tables not kept
        for (DatabaseTable tbl : existingTables) {
            if (!keptTableIds.contains(tbl.getId())) {
                // Delete related columns
                List<DatabaseColumn> cols = databaseColumnRepository.findByTableId(tbl.getId());
                databaseColumnRepository.deleteAll(cols);
                databaseTableRepository.delete(tbl);
            }
        }

        List<DatabaseRelationship> relationships = databaseRelationshipRepository.findBySchemaId(schema.getId());
        return databaseMapper.toSchemaResponse(schema, savedTables, relationships);
    }

    @Transactional
    public DatabaseRelationshipResponse createRelationship(UUID projectId, CreateRelationshipRequest request) {
        DatabaseSchema schema = getOrCreateDefaultSchema(projectId);

        DatabaseRelationship relationship = DatabaseRelationship.builder()
                .schemaId(schema.getId())
                .sourceTableId(request.getSourceTableId())
                .sourceColumnId(request.getSourceColumnId())
                .targetTableId(request.getTargetTableId())
                .targetColumnId(request.getTargetColumnId())
                .constraintName(request.getConstraintName())
                .onDeleteAction(request.getOnDeleteAction() != null ? request.getOnDeleteAction() : "NO ACTION")
                .onUpdateAction(request.getOnUpdateAction() != null ? request.getOnUpdateAction() : "NO ACTION")
                .build();

        relationship = databaseRelationshipRepository.save(relationship);
        return databaseMapper.toRelationshipResponse(relationship);
    }

    public SqlPreviewResponse generateSqlPreview(UUID projectId) {
        DatabaseSchema schema = getOrCreateDefaultSchema(projectId);
        List<DatabaseTable> tables = databaseTableRepository.findBySchemaId(schema.getId());
        String dbType = schema.getDbType() != null ? schema.getDbType().toLowerCase() : "postgresql";

        if (tables.isEmpty()) {
            return new SqlPreviewResponse("-- Chưa có bảng nào được tạo");
        }

        StringBuilder sql = new StringBuilder();
        for (DatabaseTable table : tables) {
            sql.append("CREATE TABLE ").append(quoteIdentifier(table.getName(), dbType)).append(" (\n");
            List<DatabaseColumn> columns = databaseColumnRepository.findByTableId(table.getId());

            List<String> colLines = new ArrayList<>();
            for (DatabaseColumn col : columns) {
                String typeStr = DatabaseTypeParser.format(col.getDataType(), col.getLength(), col.getPrecisionValue(), col.getScaleValue());
                StringBuilder colLine = new StringBuilder();
                colLine.append("  ").append(quoteIdentifier(col.getName(), dbType)).append(" ").append(typeStr);

                if (col.isPrimaryKey()) {
                    colLine.append(" PRIMARY KEY NOT NULL");
                } else {
                    if (col.isUnique()) {
                        colLine.append(" UNIQUE");
                    }
                    if (!col.isNullable()) {
                        colLine.append(" NOT NULL");
                    }
                }
                if (col.getDefaultValue() != null && !col.getDefaultValue().isBlank()) {
                    colLine.append(" DEFAULT ").append(col.getDefaultValue());
                }
                colLines.add(colLine.toString());
            }
            sql.append(String.join(",\n", colLines));
            sql.append("\n);\n\n");
        }

        return new SqlPreviewResponse(sql.toString().trim());
    }

    private String quoteIdentifier(String name, String dbType) {
        if ("mysql".equals(dbType)) {
            return "`" + name + "`";
        }
        if ("sqlserver".equals(dbType)) {
            return "[" + name + "]";
        }
        return "\"" + name + "\"";
    }

    private DatabaseSchema getOrCreateDefaultSchema(UUID projectId) {
        return databaseSchemaRepository.findByProjectId(projectId)
                .orElseGet(() -> {
                    DatabaseSchema newSchema = DatabaseSchema.builder()
                            .projectId(projectId)
                            .dbType("postgresql")
                            .name("default_schema")
                            .build();
                    return databaseSchemaRepository.save(newSchema);
                });
    }
}
