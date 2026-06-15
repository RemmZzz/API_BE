package com.apibe.API_BE.module.environment.dto.response;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveEnvironmentResponse {
    private UUID id;
    private UUID projectId;
    private UUID environmentId;
}
