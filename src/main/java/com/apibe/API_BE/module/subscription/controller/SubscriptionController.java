package com.apibe.API_BE.module.subscription.controller;

import com.apibe.API_BE.global.response.ApiResponse;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.module.subscription.dto.request.UpdateSubscriptionRequest;
import com.apibe.API_BE.module.subscription.dto.response.SubscriptionPlanResponse;
import com.apibe.API_BE.module.subscription.dto.response.SubscriptionResponse;
import com.apibe.API_BE.module.subscription.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/plans")
    public ApiResponse<List<SubscriptionPlanResponse>> getPlans() {
        List<SubscriptionPlanResponse> plans = subscriptionService.getAllPlans();
        return ApiResponse.success("Lấy danh sách gói dịch vụ thành công", plans);
    }

    @GetMapping("/subscription")
    public ApiResponse<SubscriptionResponse> getSubscription() {
        UUID userId = SecurityUtils.getCurrentUserId();
        SubscriptionResponse subscription = subscriptionService.getSubscriptionByUserId(userId);
        return ApiResponse.success("Lấy thông tin đăng ký dịch vụ thành công", subscription);
    }

    @PostMapping("/subscription")
    public ApiResponse<SubscriptionResponse> updateSubscription(@RequestBody UpdateSubscriptionRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        SubscriptionResponse subscription = subscriptionService.updateSubscription(userId, request);
        return ApiResponse.success("Cập nhật đăng ký dịch vụ thành công", subscription);
    }
}
