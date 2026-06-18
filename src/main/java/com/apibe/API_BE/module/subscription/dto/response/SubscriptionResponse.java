package com.apibe.API_BE.module.subscription.dto.response;

import com.apibe.API_BE.global.enums.SubscriptionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private UUID userId;
    private UUID planUuid;       // The database plan UUID
    private String planId;        // The plan code name (e.g. 'free', 'pro', 'ultra') for frontend compatibility
    private String planName;
    private BigDecimal price;
    private String cycle;
    private SubscriptionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime expiredAt;
    private String paymentOrderCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
