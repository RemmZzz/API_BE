package com.apibe.API_BE.module.database.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTableRequest {
    private String name;
    private List<CreateColumnRequest> columns;
    private Integer positionX;
    private Integer positionY;
}
