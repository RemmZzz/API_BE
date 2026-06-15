package com.apibe.API_BE.module.database.dto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSchemaResponse {
    private UUID id;
    private UUID projectId;
    private String dbType;
    private String name;
    private List<DatabaseTableResponse> tables;
    private List<DatabaseRelationshipResponse> relationships;
}
