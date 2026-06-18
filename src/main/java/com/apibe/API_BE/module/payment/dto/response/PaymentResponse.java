package com.apibe.API_BE.module.payment.dto.response;

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
public class PaymentResponse {
    private UUID id;
    private UUID userId;
    private UUID subscriptionId;
    private String orderCode;
    private String provider;
    private UUID planId;
    private String planName;
    private String cycle;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String bankName;
    private String accountName;
    private String accountNumber;
    private String transferContent;
    private String qrCodeUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
}
