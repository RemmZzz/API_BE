package com.apibe.API_BE.module.environment.dto.request;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveEnvironmentRequest {
    private UUID environmentId;
}
