package com.apibe.API_BE.module.admin.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {

    private UUID id;
    private UUID userId;
    private UUID projectId;
    private String module;
    private String category;
    private String action;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
