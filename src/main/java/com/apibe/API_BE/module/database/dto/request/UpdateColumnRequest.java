package com.apibe.API_BE.module.database.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateColumnRequest {
    private String name;
    private String type;
    private Boolean primaryKey;
    private Boolean nullable;
    private Boolean unique;
    private String defaultValue;
    private Integer ordinalPosition;
    private String comment;
}
