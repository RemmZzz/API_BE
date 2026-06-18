package com.apibe.API_BE.module.payment.service;

import com.apibe.API_BE.global.config.SePayProperties;
import com.apibe.API_BE.global.enums.PaymentStatus;
import com.apibe.API_BE.global.enums.SubscriptionStatus;
import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.module.payment.dto.request.CreatePaymentRequest;
import com.apibe.API_BE.module.payment.dto.request.SePayWebhookRequest;
import com.apibe.API_BE.module.payment.dto.response.PaymentResponse;
import com.apibe.API_BE.module.payment.entity.Payment;
import com.apibe.API_BE.module.payment.entity.PaymentEvent;
import com.apibe.API_BE.module.payment.repository.PaymentEventRepository;
import com.apibe.API_BE.module.payment.repository.PaymentRepository;
import com.apibe.API_BE.module.subscription.dto.request.UpdateSubscriptionRequest;
import com.apibe.API_BE.module.subscription.dto.response.SubscriptionResponse;
import com.apibe.API_BE.module.subscription.entity.SubscriptionPlan;
import com.apibe.API_BE.module.subscription.repository.SubscriptionPlanRepository;
import com.apibe.API_BE.module.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionService subscriptionService;
    private final SePayProperties sePayProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentResponse createPayment(UUID userId, CreatePaymentRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByCode(request.getPlanCode().toLowerCase())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Subscription plan not found"));

        if (!plan.isActive()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Subscription plan is not active");
        }

        String cycle = request.getCycle().toLowerCase();
        BigDecimal amount;
        if ("yearly".equals(cycle)) {
            amount = plan.getPriceYearly();
        } else if ("monthly".equals(cycle)) {
            amount = plan.getPriceMonthly();
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Cycle must be monthly or yearly");
        }

        // Generate unique orderCode with random 4-digit suffix to prevent collisions at the same millisecond
        String orderCode = "PAY" + System.currentTimeMillis() + String.format("%04d", new java.security.SecureRandom().nextInt(10000));

        Payment payment = Payment.builder()
                .userId(userId)
                .orderCode(orderCode)
                .provider("BANK_TRANSFER")
                .bankName(sePayProperties.getBankId())
                .accountName(sePayProperties.getAccountName())
                .accountNumber(sePayProperties.getAccountNumber())
                .transferContent(orderCode)
                .planId(plan.getId())
                .planName(plan.getName())
                .cycle(cycle)
                .amount(amount)
                .currency("VND")
                .status(PaymentStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse confirmPayment(String orderCode) {
        Payment payment = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Payment is not in PENDING status");
        }

        if (LocalDateTime.now().isAfter(payment.getExpiredAt())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            throw new AppException(ErrorCode.INVALID_REQUEST, "Payment has expired");
        }

        // Activate user subscription
        activateSubscriptionForPayment(payment);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        // Record payment event
        savePaymentEvent(payment.getId(), "MOCK_CONFIRM", "{\"confirmedBy\":\"USER\"}");

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse cancelPayment(String orderCode) {
        Payment payment = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Only PENDING payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCancelledAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public void processSePayWebhook(String authorizationHeader, SePayWebhookRequest webhookRequest) {
        log.info("Received SePay webhook: {}", webhookRequest);

        // Authenticate webhook
        String expectedHeader = "Apikey " + sePayProperties.getApiKey();
        if (StringUtils.hasText(sePayProperties.getApiKey()) && 
                (authorizationHeader == null || !authorizationHeader.equalsIgnoreCase(expectedHeader))) {
            log.error("Invalid SePay webhook API key");
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid Webhook API Key");
        }

        // Find payment
        Payment payment = paymentRepository.findByOrderCode(webhookRequest.getCode()).orElse(null);
        if (payment == null && webhookRequest.getContent() != null) {
            // Trích xuất mã đơn hàng từ nội dung chuyển khoản bằng Regex để tránh tải toàn bộ PENDING vào bộ nhớ
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("PAY\\d{13,22}");
            java.util.regex.Matcher matcher = pattern.matcher(webhookRequest.getContent());
            if (matcher.find()) {
                String extractedCode = matcher.group();
                payment = paymentRepository.findByOrderCode(extractedCode).orElse(null);
            }
        }

        if (payment == null) {
            log.error("Payment not found for webhook request code: {} content: {}", webhookRequest.getCode(), webhookRequest.getContent());
            throw new AppException(ErrorCode.NOT_FOUND, "Payment not found for order code");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Payment {} is already processed (status: {})", payment.getOrderCode(), payment.getStatus());
            return;
        }

        if (LocalDateTime.now().isAfter(payment.getExpiredAt())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            log.error("Payment {} has expired", payment.getOrderCode());
            throw new AppException(ErrorCode.INVALID_REQUEST, "Payment has expired");
        }

        if (webhookRequest.getTransferAmount().compareTo(payment.getAmount()) < 0) {
            log.error("Payment {} transfer amount {} is less than required amount {}", 
                    payment.getOrderCode(), webhookRequest.getTransferAmount(), payment.getAmount());
            throw new AppException(ErrorCode.INVALID_REQUEST, "Transfer amount is insufficient");
        }

        // Activate subscription
        activateSubscriptionForPayment(payment);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Record event
        String payloadJson = "{}";
        try {
            payloadJson = objectMapper.writeValueAsString(webhookRequest);
        } catch (Exception e) {
            log.error("Failed to serialize webhook request payload", e);
        }
        savePaymentEvent(payment.getId(), "SEPAY_WEBHOOK", payloadJson);

        log.info("Successfully processed SePay webhook for payment {}", payment.getOrderCode());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentHistory(UUID userId, PaymentStatus status) {
        List<Payment> payments;
        if (status != null) {
            payments = paymentRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else {
            payments = paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        }
        return payments.stream().map(this::mapToPaymentResponse).collect(Collectors.toList());
    }

    private void activateSubscriptionForPayment(Payment payment) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(payment.getPlanId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "Subscription plan details not found"));

        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime expiredAt = "yearly".equalsIgnoreCase(payment.getCycle()) 
                ? startedAt.plusYears(1) 
                : startedAt.plusMonths(1);

        UpdateSubscriptionRequest updateRequest = UpdateSubscriptionRequest.builder()
                .planId(plan.getCode())
                .planName(payment.getPlanName())
                .price(payment.getAmount())
                .cycle(payment.getCycle())
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(startedAt)
                .expiredAt(expiredAt)
                .paymentOrderCode(payment.getOrderCode())
                .build();

        SubscriptionResponse subResponse = subscriptionService.updateSubscription(payment.getUserId(), updateRequest);
        payment.setSubscriptionId(subResponse.getId());
    }

    private void savePaymentEvent(UUID paymentId, String eventType, String payloadJson) {
        try {
            PaymentEvent event = PaymentEvent.builder()
                    .paymentId(paymentId)
                    .eventType(eventType)
                    .payloadJson(payloadJson)
                    .build();
            paymentEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to save payment event log", e);
        }
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        String qrCodeUrl = null;
        if (payment.getBankName() != null && payment.getAccountNumber() != null) {
            qrCodeUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
                    payment.getBankName(),
                    payment.getAccountNumber(),
                    payment.getAmount().toBigInteger().toString(),
                    URLEncoder.encode(payment.getTransferContent(), StandardCharsets.UTF_8),
                    URLEncoder.encode(payment.getAccountName(), StandardCharsets.UTF_8)
            );
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .subscriptionId(payment.getSubscriptionId())
                .orderCode(payment.getOrderCode())
                .provider(payment.getProvider())
                .planId(payment.getPlanId())
                .planName(payment.getPlanName())
                .cycle(payment.getCycle())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .bankName(payment.getBankName())
                .accountName(payment.getAccountName())
                .accountNumber(payment.getAccountNumber())
                .transferContent(payment.getTransferContent())
                .qrCodeUrl(qrCodeUrl)
                .createdAt(payment.getCreatedAt())
                .expiredAt(payment.getExpiredAt())
                .paidAt(payment.getPaidAt())
                .cancelledAt(payment.getCancelledAt())
                .build();
    }
}
