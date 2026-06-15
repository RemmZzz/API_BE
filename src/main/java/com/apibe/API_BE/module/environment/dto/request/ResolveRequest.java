package com.apibe.API_BE.module.environment.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveRequest {
    private String text;
}
