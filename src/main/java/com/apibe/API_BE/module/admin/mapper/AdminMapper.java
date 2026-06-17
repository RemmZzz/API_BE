package com.apibe.API_BE.module.admin.mapper;

import com.apibe.API_BE.module.activity.entity.ActivityLog;
import com.apibe.API_BE.module.admin.dto.response.*;
import com.apibe.API_BE.module.admin.repository.projection.CountByStatusProjection;
import com.apibe.API_BE.module.admin.repository.projection.RevenueItemProjection;
import com.apibe.API_BE.module.payment.entity.Payment;
import com.apibe.API_BE.module.user.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AdminMapper {

    public AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .status(user.getStatus())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public CountByStatusResponse toCountByStatusResponse(CountByStatusProjection projection) {
        return CountByStatusResponse.builder()
                .status(projection.getStatus())
                .count(projection.getCount())
                .build();
    }

    public RecentActivityResponse toRecentActivityResponse(ActivityLog activityLog) {
        return RecentActivityResponse.builder()
                .id(activityLog.getId())
                .userId(activityLog.getUserId())
                .projectId(activityLog.getProjectId())
                .module(activityLog.getModule())
                .category(activityLog.getCategory())
                .action(activityLog.getAction())
                .description(activityLog.getDescription())
                .status(activityLog.getStatus())
                .createdAt(activityLog.getCreatedAt())
                .build();
    }

    public RecentPaymentResponse toRecentPaymentResponse(Payment payment) {
        return RecentPaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .orderCode(payment.getOrderCode())
                .provider(payment.getProvider())
                .planName(payment.getPlanName())
                .cycle(payment.getCycle())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }

    public RevenueItemResponse toRevenueItemResponse(RevenueItemProjection projection) {
        return RevenueItemResponse.builder()
                .period(projection.getPeriod())
                .revenue(projection.getRevenue() == null ? BigDecimal.ZERO : projection.getRevenue())
                .paymentCount(projection.getPaymentCount())
                .build();
    }
}
