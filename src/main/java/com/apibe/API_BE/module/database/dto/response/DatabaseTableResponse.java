package com.apibe.API_BE.module.database.dto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseTableResponse {
    private UUID id;
    private String name;
    private String displayName;
    private int rowCount;
    private Integer positionX;
    private Integer positionY;
    private List<DatabaseColumnResponse> columns;
}
