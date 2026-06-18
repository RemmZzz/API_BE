package com.apibe.API_BE.module.subscription.service;

import com.apibe.API_BE.module.subscription.entity.SubscriptionPlan;
import com.apibe.API_BE.module.subscription.repository.SubscriptionPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(1)
@SuppressWarnings("null")
public class SubscriptionPlanSeeder implements CommandLineRunner {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanSeeder(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (subscriptionPlanRepository.count() == 0) {
            SubscriptionPlan freePlan = SubscriptionPlan.builder()
                    .code("free")
                    .name("Free")
                    .description("Gói miễn phí cho người dùng mới")
                    .priceMonthly(BigDecimal.ZERO)
                    .priceYearly(BigDecimal.ZERO)
                    .currency("VND")
                    .limitsJson("{\"projects\":1,\"apiRequestsPerDay\":100,\"aiMessagesPerMonth\":100,\"storageMb\":100}")
                    .featuresJson("[\"1 Project\",\"100 API Requests/day\",\"100 AI Messages/month\",\"100MB Storage\"]")
                    .isActive(true)
                    .build();

            SubscriptionPlan proPlan = SubscriptionPlan.builder()
                    .code("pro")
                    .name("Pro")
                    .description("Gói nâng cao cho người dùng chuyên nghiệp")
                    .priceMonthly(new BigDecimal("199999"))
                    .priceYearly(new BigDecimal("1999990"))
                    .currency("VND")
                    .limitsJson("{\"projects\":10,\"apiRequestsPerDay\":5000,\"aiMessagesPerMonth\":5000,\"storageMb\":2048}")
                    .featuresJson("[\"10 Projects\",\"5000 API Requests/day\",\"5000 AI Messages/month\",\"2GB Storage\"]")
                    .isActive(true)
                    .build();

            SubscriptionPlan ultraPlan = SubscriptionPlan.builder()
                    .code("ultra")
                    .name("Ultra")
                    .description("Gói cao cấp với đầy đủ tính năng")
                    .priceMonthly(new BigDecimal("999999"))
                    .priceYearly(new BigDecimal("9999990"))
                    .currency("VND")
                    .limitsJson("{\"projects\":-1,\"apiRequestsPerDay\":-1,\"aiMessagesPerMonth\":-1,\"storageMb\":10240}")
                    .featuresJson("[\"Không giới hạn Projects\",\"Không giới hạn API Requests\",\"Không giới hạn AI Messages\",\"10GB Storage\"]")
                    .isActive(true)
                    .build();

            subscriptionPlanRepository.saveAll(List.of(freePlan, proPlan, ultraPlan));
            System.out.println(">> Subscription plans have been successfully seeded!");
        }
    }
}
