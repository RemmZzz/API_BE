package com.apibe.API_BE.module.subscription.service;

import com.apibe.API_BE.global.enums.SubscriptionStatus;
import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.subscription.dto.request.UpdateSubscriptionRequest;
import com.apibe.API_BE.module.subscription.dto.response.SubscriptionPlanResponse;
import com.apibe.API_BE.module.subscription.dto.response.SubscriptionResponse;
import com.apibe.API_BE.module.subscription.entity.Subscription;
import com.apibe.API_BE.module.subscription.entity.SubscriptionPlan;
import com.apibe.API_BE.module.subscription.repository.SubscriptionPlanRepository;
import com.apibe.API_BE.module.subscription.repository.SubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class SubscriptionService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public SubscriptionService(SubscriptionPlanRepository subscriptionPlanRepository,
                               SubscriptionRepository subscriptionRepository,
                               ObjectMapper objectMapper) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    public List<SubscriptionPlanResponse> getAllPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllByIsActiveTrue();
        return plans.stream().map(this::mapToPlanResponse).collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionResponse getSubscriptionByUserId(UUID userId) {
        // Query the latest active subscription
        Subscription activeSub = subscriptionRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, SubscriptionStatus.ACTIVE)
                .orElse(null);

        if (activeSub == null) {
            // Check if user has any subscription at all (e.g. INACTIVE/CANCELLED/EXPIRED)
            // If none, auto-create a Free plan subscription
            SubscriptionPlan freePlan = subscriptionPlanRepository.findByCode("free")
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Free plan details not found."));

            activeSub = Subscription.builder()
                    .userId(userId)
                    .planId(freePlan.getId())
                    .planName(freePlan.getName())
                    .price(freePlan.getPriceMonthly())
                    .cycle("monthly")
                    .status(SubscriptionStatus.ACTIVE)
                    .startedAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.now().plusMonths(1))
                    .build();

            activeSub = subscriptionRepository.save(activeSub);
        }

        return mapToSubscriptionResponse(activeSub);
    }

    @Transactional
    public SubscriptionResponse updateSubscription(UUID userId, UpdateSubscriptionRequest request) {
        String planCode = request.getPlanId();
        if (planCode == null) {
            planCode = "free";
        }
        
        SubscriptionPlan plan = subscriptionPlanRepository.findByCode(planCode.toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Plan not found with code: " + request.getPlanId()));

        // Deactivate existing active subscriptions for this user
        subscriptionRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, SubscriptionStatus.ACTIVE)
                .ifPresent(existingSub -> {
                    existingSub.setStatus(SubscriptionStatus.INACTIVE);
                    subscriptionRepository.save(existingSub);
                });

        // Determine started and expired times
        LocalDateTime startedAt = request.getStartedAt() != null ? request.getStartedAt() : LocalDateTime.now();
        LocalDateTime expiredAt = request.getExpiredAt();
        if (expiredAt == null && !"free".equalsIgnoreCase(plan.getCode())) {
            expiredAt = startedAt.plusMonths(1);
        }

        Subscription newSub = Subscription.builder()
                .userId(userId)
                .planId(plan.getId())
                .planName(request.getPlanName() != null ? request.getPlanName() : plan.getName())
                .price(request.getPrice() != null ? request.getPrice() : plan.getPriceMonthly())
                .cycle(request.getCycle() != null ? request.getCycle() : "monthly")
                .status(request.getStatus() != null ? request.getStatus() : SubscriptionStatus.ACTIVE)
                .startedAt(startedAt)
                .activatedAt(startedAt)
                .expiredAt(expiredAt)
                .paymentOrderCode(request.getPaymentOrderCode())
                .build();

        newSub = subscriptionRepository.save(newSub);
        return mapToSubscriptionResponse(newSub);
    }

    private SubscriptionPlanResponse mapToPlanResponse(SubscriptionPlan plan) {
        Object limits = null;
        Object features = null;

        try {
            if (plan.getLimitsJson() != null) {
                limits = objectMapper.readValue(plan.getLimitsJson(), Map.class);
            }
        } catch (Exception e) {
            limits = Map.of();
        }

        try {
            if (plan.getFeaturesJson() != null) {
                features = objectMapper.readValue(plan.getFeaturesJson(), List.class);
            }
        } catch (Exception e) {
            features = List.of();
        }

        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .priceMonthly(plan.getPriceMonthly())
                .priceYearly(plan.getPriceYearly())
                .currency(plan.getCurrency())
                .limits(limits)
                .features(features)
                .isActive(plan.isActive())
                .build();
    }

    private SubscriptionResponse mapToSubscriptionResponse(Subscription sub) {
        String planCode = "free";
        SubscriptionPlan plan = subscriptionPlanRepository.findById(sub.getPlanId()).orElse(null);
        if (plan != null) {
            planCode = plan.getCode();
        }

        return SubscriptionResponse.builder()
                .id(sub.getId())
                .userId(sub.getUserId())
                .planUuid(sub.getPlanId())
                .planId(planCode)
                .planName(sub.getPlanName())
                .price(sub.getPrice())
                .cycle(sub.getCycle())
                .status(sub.getStatus())
                .startedAt(sub.getStartedAt())
                .activatedAt(sub.getActivatedAt())
                .expiredAt(sub.getExpiredAt())
                .paymentOrderCode(sub.getPaymentOrderCode())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }
}
