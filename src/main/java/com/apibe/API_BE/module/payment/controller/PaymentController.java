package com.apibe.API_BE.module.payment.controller;

import com.apibe.API_BE.global.enums.PaymentStatus;
import com.apibe.API_BE.global.response.ApiResponse;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.module.payment.dto.request.CreatePaymentRequest;
import com.apibe.API_BE.module.payment.dto.request.SePayWebhookRequest;
import com.apibe.API_BE.module.payment.dto.response.PaymentResponse;
import com.apibe.API_BE.module.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        PaymentResponse response = paymentService.createPayment(userId, request);
        return ApiResponse.success("Tạo đơn hàng thanh toán thành công", response);
    }

    @PostMapping("/payments/{orderCode}/confirm")
    public ApiResponse<PaymentResponse> confirmPayment(@PathVariable String orderCode) {
        PaymentResponse response = paymentService.confirmPayment(orderCode);
        return ApiResponse.success("Xác nhận thanh toán đơn hàng thành công", response);
    }

    @PostMapping("/payments/{orderCode}/cancel")
    public ApiResponse<PaymentResponse> cancelPayment(@PathVariable String orderCode) {
        PaymentResponse response = paymentService.cancelPayment(orderCode);
        return ApiResponse.success("Hủy đơn hàng thanh toán thành công", response);
    }

    @GetMapping("/payments/{orderCode}")
    public ApiResponse<PaymentResponse> getPaymentByOrderCode(@PathVariable String orderCode) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<PaymentResponse> history = paymentService.getPaymentHistory(userId, null);
        PaymentResponse response = history.stream()
                .filter(p -> orderCode.equals(p.getOrderCode()))
                .findFirst()
                .orElseThrow(() -> new com.apibe.API_BE.global.exception.AppException(
                        com.apibe.API_BE.global.exception.ErrorCode.NOT_FOUND, "Payment not found"));
        return ApiResponse.success("Lấy thông tin thanh toán thành công", response);
    }

    @GetMapping("/payment-history")
    public ApiResponse<List<PaymentResponse>> getPaymentHistory(
            @RequestParam(required = false) PaymentStatus status) {
        UUID userId = SecurityUtils.getCurrentUserId();
        List<PaymentResponse> response = paymentService.getPaymentHistory(userId, status);
        return ApiResponse.success("Lấy lịch sử giao dịch thành công", response);
    }

    @PostMapping("/payments/webhook/sepay")
    public ResponseEntity<Map<String, Boolean>> handleSePayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody SePayWebhookRequest request) {
        paymentService.processSePayWebhook(authorization, request);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
