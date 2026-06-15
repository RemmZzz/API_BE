package com.apibe.API_BE.module.admin.dto.response;

import com.apibe.API_BE.global.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentPaymentResponse {

    private UUID id;
    private UUID userId;
    private String orderCode;
    private String provider;
    private String planName;
    private String cycle;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
