package com.apibe.API_BE.module.admin.dto.request;

import com.apibe.API_BE.global.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {

    @NotNull
    private UserStatus status;

    @Size(max = 500)
    private String reason;
}
