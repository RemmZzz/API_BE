package com.apibe.API_BE.module.auth.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    private String placeholder;
}

