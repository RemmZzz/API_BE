package com.apibe.API_BE.module.database.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColumnTypeInfo {
    private String dataType;
    private Integer length;
    private Integer precisionValue;
    private Integer scaleValue;
}
