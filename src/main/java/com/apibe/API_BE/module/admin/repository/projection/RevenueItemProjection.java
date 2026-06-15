package com.apibe.API_BE.module.admin.repository.projection;

import java.math.BigDecimal;

public interface RevenueItemProjection {

    String getPeriod();

    BigDecimal getRevenue();

    long getPaymentCount();
}
