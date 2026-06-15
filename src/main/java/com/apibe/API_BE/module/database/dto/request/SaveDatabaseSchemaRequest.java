package com.apibe.API_BE.module.database.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveDatabaseSchemaRequest {
    private String dbType;
    private String name;
    private List<CreateTableRequest> tables;
}
