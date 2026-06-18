package com.apibe.API_BE.module.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    @NotBlank(message = "Plan code is required")
    private String planCode;

    @NotBlank(message = "Cycle is required")
    private String cycle;
}
