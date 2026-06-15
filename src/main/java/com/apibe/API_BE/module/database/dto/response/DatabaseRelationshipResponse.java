package com.apibe.API_BE.module.database.dto.response;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseRelationshipResponse {
    private UUID id;
    private UUID sourceTableId;
    private UUID sourceColumnId;
    private UUID targetTableId;
    private UUID targetColumnId;
    private String constraintName;
    private String onDeleteAction;
    private String onUpdateAction;
}
