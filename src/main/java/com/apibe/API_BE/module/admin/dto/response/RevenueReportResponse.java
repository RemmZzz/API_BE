package com.apibe.API_BE.module.admin.dto.response;

import com.apibe.API_BE.global.response.PageResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {

    private BigDecimal totalRevenue;
    private long totalPayments;
    private long successPayments;
    private long failedPayments;
    private long pendingPayments;
    private List<RevenueItemResponse> revenueItems;
    private PageResponse<RecentPaymentResponse> payments;
}
