package com.apibe.API_BE.module.workspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateConversationRequest {

    private String title;

    @NotBlank(message = "Chế độ hội thoại không được để trống")
    private String mode;
}
