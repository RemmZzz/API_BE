package com.apibe.API_BE.module.project.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {

    private String name;
    private String description;
    private String type;
    private String color;
    private String status;

    @AssertTrue(message = "Tên dự án không được để trống")
    public boolean isNameValidWhenPresent() {
        return name == null || !name.isBlank();
    }
}
