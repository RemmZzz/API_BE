package com.apibe.API_BE.module.admin.dto.request;

import com.apibe.API_BE.global.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {

    @NotNull
    private UserRole role;

    @Size(max = 500)
    private String reason;
}
