package com.apibe.API_BE.module.admin.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewResponse {

    private long totalUsers;
    private long totalProjects;
    private BigDecimal totalRevenue;
    private long totalApiCalls;
    private long totalAiConversations;
    private List<CountByStatusResponse> usersByStatus;
    private List<CountByStatusResponse> projectsByStatus;
    private List<CountByStatusResponse> paymentsByStatus;
    private List<RecentActivityResponse> recentActivities;
    private List<RecentPaymentResponse> recentPayments;
}
