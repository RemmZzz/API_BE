package com.apibe.API_BE.module.environment.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentResponse {
    private UUID id;
    private UUID projectId;
    private String name;
    private String description;
    private List<EnvironmentVariableResponse> variables;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
