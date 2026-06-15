package com.apibe.API_BE.module.database.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateColumnRequest {
    private String name;
    private String type;
    private boolean primaryKey;
    private boolean nullable;
    private boolean unique;
    private String defaultValue;
    private Integer ordinalPosition;
    private String comment;
}
