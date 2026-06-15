package com.apibe.API_BE.module.environment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnvironmentRequest {
    @NotBlank
    private String name;
    private String description;
}
