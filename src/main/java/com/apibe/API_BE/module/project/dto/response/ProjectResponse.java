package com.apibe.API_BE.module.project.dto.response;

import com.apibe.API_BE.global.enums.ProjectStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private String type;
    private ProjectStatus status;
    private List<String> tags;
    private List<String> tech;
    private int apiCount;
    private int databaseTableCount;
    private int aiChatCount;
    private String color;
    private UUID ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
