package com.apibe.API_BE.module.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    @NotBlank(message = "Tên dự án không được để trống")
    private String name;

    private String description;
    private String type;
    private String color;
}
