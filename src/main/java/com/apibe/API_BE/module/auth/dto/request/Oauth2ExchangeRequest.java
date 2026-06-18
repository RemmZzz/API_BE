package com.apibe.API_BE.module.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Oauth2ExchangeRequest {

    @NotBlank(message = "Exchange code is required")
    private String code;
}
