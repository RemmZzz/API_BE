package com.apibe.API_BE.module.database.mapper;

import com.apibe.API_BE.module.database.dto.response.*;
import com.apibe.API_BE.module.database.entity.*;
import com.apibe.API_BE.module.database.util.DatabaseTypeParser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatabaseMapper {

    public DatabaseColumnResponse toColumnResponse(DatabaseColumn column) {
        if (column == null) {
            return null;
        }
        return DatabaseColumnResponse.builder()
                .id(column.getId())
                .name(column.getName())
                .type(DatabaseTypeParser.format(
                        column.getDataType(),
                        column.getLength(),
                        column.getPrecisionValue(),
                        column.getScaleValue()
                ))
                .primaryKey(column.isPrimaryKey())
                .nullable(column.isNullable())
                .unique(column.isUnique())
                .defaultValue(column.getDefaultValue())
                .ordinalPosition(column.getOrdinalPosition())
                .comment(column.getComment())
                .build();
    }

    public DatabaseTableResponse toTableResponse(DatabaseTable table, List<DatabaseColumn> columns) {
        if (table == null) {
            return null;
        }
        List<DatabaseColumnResponse> colResponses = null;
        if (columns != null) {
            colResponses = columns.stream()
                    .map(this::toColumnResponse)
                    .collect(Collectors.toList());
        }
        return DatabaseTableResponse.builder()
                .id(table.getId())
                .name(table.getName())
                .displayName(table.getDisplayName())
                .rowCount(table.getRowCount())
                .positionX(table.getPositionX())
                .positionY(table.getPositionY())
                .columns(colResponses)
                .build();
    }

    public DatabaseRelationshipResponse toRelationshipResponse(DatabaseRelationship relationship) {
        if (relationship == null) {
            return null;
        }
        return DatabaseRelationshipResponse.builder()
                .id(relationship.getId())
                .sourceTableId(relationship.getSourceTableId())
                .sourceColumnId(relationship.getSourceColumnId())
                .targetTableId(relationship.getTargetTableId())
                .targetColumnId(relationship.getTargetColumnId())
                .constraintName(relationship.getConstraintName())
                .onDeleteAction(relationship.getOnDeleteAction())
                .onUpdateAction(relationship.getOnUpdateAction())
                .build();
    }

    public DatabaseSchemaResponse toSchemaResponse(DatabaseSchema schema,
                                                   List<DatabaseTableResponse> tables,
                                                   List<DatabaseRelationship> relationships) {
        if (schema == null) {
            return null;
        }
        List<DatabaseRelationshipResponse> relResponses = null;
        if (relationships != null) {
            relResponses = relationships.stream()
                    .map(this::toRelationshipResponse)
                    .collect(Collectors.toList());
        }
        return DatabaseSchemaResponse.builder()
                .id(schema.getId())
                .projectId(schema.getProjectId())
                .dbType(schema.getDbType())
                .name(schema.getName())
                .tables(tables)
                .relationships(relResponses)
                .build();
    }
}
