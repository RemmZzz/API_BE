package com.apibe.API_BE.module.payment.entity;

import com.apibe.API_BE.global.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private UUID userId;

    @Column(name = "subscription_id", columnDefinition = "CHAR(36)")
    private UUID subscriptionId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "provider")
    private String provider;

    @Column(name = "plan_id")
    private String planId;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "cycle")
    private String cycle;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
