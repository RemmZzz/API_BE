package com.apibe.API_BE.module.environment.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentVariableResponse {
    private UUID id;
    private UUID environmentId;
    private String key;
    private String initialValue;
    private String currentValue;
    private String type;
    private boolean isEnabled;
    private boolean isSecret;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
