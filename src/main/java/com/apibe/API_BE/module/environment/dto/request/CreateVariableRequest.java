package com.apibe.API_BE.module.environment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariableRequest {
    @NotBlank
    private String key;
    private String initialValue;
    private String currentValue;
    private String type;
    private Boolean isEnabled;
    private Boolean isSecret;
}
