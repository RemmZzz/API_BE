package com.apibe.API_BE.module.subscription.dto.request;

import com.apibe.API_BE.global.enums.SubscriptionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionRequest {
    private String planId; // The plan code name (e.g. 'free', 'pro', 'ultra')
    private String planName;
    private BigDecimal price;
    private String cycle;
    private SubscriptionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;
    private String paymentOrderCode;
}
