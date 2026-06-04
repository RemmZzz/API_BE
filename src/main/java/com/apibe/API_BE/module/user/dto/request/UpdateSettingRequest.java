package com.apibe.API_BE.module.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingRequest {

    @Pattern(regexp = "^(vi|en)$", message = "Ngôn ngữ không hợp lệ")
    private String language;
    private Object notificationSettings;
    private Object privacySettings;
}
