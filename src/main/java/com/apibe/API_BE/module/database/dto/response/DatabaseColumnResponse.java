package com.apibe.API_BE.module.database.dto.response;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseColumnResponse {
    private UUID id;
    private String name;
    private String type;
    private boolean primaryKey;
    private boolean nullable;
    private boolean unique;
    private String defaultValue;
    private int ordinalPosition;
    private String comment;
}
