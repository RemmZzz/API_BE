package com.apibe.API_BE.module.collection.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollectionRequest {

    @NotBlank(message = "Collection name is required")
    private String name;

    private String description;
}
