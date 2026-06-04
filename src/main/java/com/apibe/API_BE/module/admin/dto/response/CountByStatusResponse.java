package com.apibe.API_BE.module.admin.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountByStatusResponse {

    private String status;
    private long count;
}
