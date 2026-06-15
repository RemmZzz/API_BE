package com.apibe.API_BE.module.admin.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueItemResponse {

    private String period;
    private BigDecimal revenue;
    private long paymentCount;
}
