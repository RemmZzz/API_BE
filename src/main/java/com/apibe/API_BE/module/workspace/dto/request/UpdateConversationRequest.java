package com.apibe.API_BE.module.workspace.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateConversationRequest {
    private String title;
    private Boolean pinned;
    private Boolean archived;
}
