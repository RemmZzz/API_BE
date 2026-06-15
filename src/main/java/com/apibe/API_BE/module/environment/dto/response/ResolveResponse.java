package com.apibe.API_BE.module.environment.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveResponse {
    private String resolvedText;
}
