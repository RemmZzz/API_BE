package com.apibe.API_BE.payment;

import com.apibe.API_BE.global.config.SePayProperties;
import com.apibe.API_BE.global.enums.PaymentStatus;
import com.apibe.API_BE.global.enums.SubscriptionStatus;
import com.apibe.API_BE.module.admin.repository.*;
import com.apibe.API_BE.module.apitester.repository.ApiTestHistoryRepository;
import com.apibe.API_BE.module.collection.repository.*;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.environment.repository.*;
import com.apibe.API_BE.module.mockserver.repository.MockEndpointRepository;
import com.apibe.API_BE.module.payment.dto.request.CreatePaymentRequest;
import com.apibe.API_BE.module.payment.dto.request.SePayWebhookRequest;
import com.apibe.API_BE.module.payment.entity.Payment;
import com.apibe.API_BE.module.payment.repository.PaymentEventRepository;
import com.apibe.API_BE.module.payment.repository.PaymentRepository;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.subscription.entity.Subscription;
import com.apibe.API_BE.module.subscription.entity.SubscriptionPlan;
import com.apibe.API_BE.module.subscription.repository.SubscriptionPlanRepository;
import com.apibe.API_BE.module.subscription.repository.SubscriptionRepository;
import com.apibe.API_BE.module.user.repository.*;
import com.apibe.API_BE.module.workspace.repository.AiConversationRepository;
import com.apibe.API_BE.module.workspace.repository.AiMessageRepository;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SePayProperties sePayProperties;

    @MockitoBean private PaymentRepository paymentRepository;
    @MockitoBean private PaymentEventRepository paymentEventRepository;
    @MockitoBean private SubscriptionPlanRepository subscriptionPlanRepository;
    @MockitoBean private SubscriptionRepository subscriptionRepository;
    @MockitoBean private ProjectRepository projectRepository;
    @MockitoBean private WorkspaceRepository workspaceRepository;
    @MockitoBean private AiConversationRepository aiConversationRepository;
    @MockitoBean private AiMessageRepository aiMessageRepository;
    @MockitoBean private MockEndpointRepository mockEndpointRepository;

    // Context Loading Mocks
    @MockitoBean private com.apibe.API_BE.module.documentation.repository.ApiDocumentationRepository apiDocumentationRepository;
    @MockitoBean private com.apibe.API_BE.module.documentation.repository.ApiDocumentationEndpointRepository apiDocumentationEndpointRepository;
    @MockitoBean private AdminUserRepository adminUserRepository;
    @MockitoBean private AdminProjectRepository adminProjectRepository;
    @MockitoBean private AdminPaymentRepository adminPaymentRepository;
    @MockitoBean private AdminApiTestHistoryRepository adminApiTestHistoryRepository;
    @MockitoBean private ApiTestHistoryRepository apiTestHistoryRepository;
    @MockitoBean private AdminAiConversationRepository adminAiConversationRepository;
    @MockitoBean private AdminActivityLogRepository adminActivityLogRepository;
    @MockitoBean private AdminAuditLogRepository adminAuditLogRepository;
    @MockitoBean private ApiCollectionRepository apiCollectionRepository;
    @MockitoBean private CollectionRepository collectionRepository;
    @MockitoBean private CollectionFolderRepository collectionFolderRepository;
    @MockitoBean private ApiFolderRepository apiFolderRepository;
    @MockitoBean private ApiRequestRepository apiRequestRepository;
    @MockitoBean private ActiveEnvironmentRepository activeEnvironmentRepository;
    @MockitoBean private EnvironmentRepository environmentRepository;
    @MockitoBean private EnvironmentVariableRepository environmentVariableRepository;
    @MockitoBean private ProjectMemberRepository projectMemberRepository;
    @MockitoBean private OtpVerificationRepository otpVerificationRepository;
    @MockitoBean private PasswordResetTokenRepository passwordResetTokenRepository;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private UserSessionRepository userSessionRepository;
    @MockitoBean private UserSettingRepository userSettingRepository;
    @MockitoBean private Oauth2ExchangeCodeRepository oauth2ExchangeCodeRepository;
    @MockitoBean private JdbcTemplate jdbcTemplate;
    @MockitoBean private com.apibe.API_BE.module.workspace.service.GeminiServiceClient geminiServiceClient;

    @MockitoBean private DatabaseSchemaRepository databaseSchemaRepository;
    @MockitoBean private DatabaseTableRepository databaseTableRepository;
    @MockitoBean private DatabaseColumnRepository databaseColumnRepository;
    @MockitoBean private DatabaseRelationshipRepository databaseRelationshipRepository;
    @MockitoBean private DatabaseIndexRepository databaseIndexRepository;

    private UUID userId;
    private UUID planId;
    private SubscriptionPlan proPlan;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        planId = UUID.randomUUID();

        // Setup Security context
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(authentication.getName()).thenReturn(userId.toString());
        Mockito.when(authentication.getPrincipal()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        proPlan = SubscriptionPlan.builder()
                .id(planId)
                .code("pro")
                .name("Pro")
                .description("Pro Plan")
                .priceMonthly(BigDecimal.valueOf(100000))
                .priceYearly(BigDecimal.valueOf(1000000))
                .currency("VND")
                .isActive(true)
                .build();

        Mockito.when(subscriptionPlanRepository.findByCode("pro")).thenReturn(Optional.of(proPlan));
        Mockito.when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(proPlan));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreatePayment_Success() throws Exception {
        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .planCode("pro")
                .cycle("monthly")
                .build();

        Mockito.when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setCreatedAt(LocalDateTime.now());
            return p;
        });

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.planName").value("Pro"))
                .andExpect(jsonPath("$.data.amount").value(100000))
                .andExpect(jsonPath("$.data.cycle").value("monthly"))
                .andExpect(jsonPath("$.data.qrCodeUrl").value(org.hamcrest.Matchers.containsString("https://img.vietqr.io/image/")));
    }

    @Test
    void testConfirmPayment_Success() throws Exception {
        String orderCode = "PAY12345";
        Payment pendingPayment = Payment.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderCode(orderCode)
                .planId(planId)
                .planName("Pro")
                .cycle("monthly")
                .amount(BigDecimal.valueOf(100000))
                .status(PaymentStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();

        Subscription mockSub = Subscription.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .planId(planId)
                .planName("Pro")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Mockito.when(paymentRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(pendingPayment));
        Mockito.when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSub);
        Mockito.when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/payments/{orderCode}/confirm", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.subscriptionId").value(mockSub.getId().toString()));
    }

    @Test
    void testCancelPayment_Success() throws Exception {
        String orderCode = "PAY12345";
        Payment pendingPayment = Payment.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderCode(orderCode)
                .status(PaymentStatus.PENDING)
                .build();

        Mockito.when(paymentRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(pendingPayment));
        Mockito.when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/payments/{orderCode}/cancel", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void testGetPaymentHistory_Success() throws Exception {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderCode("PAY123")
                .amount(BigDecimal.valueOf(100000))
                .status(PaymentStatus.SUCCESS)
                .build();

        Mockito.when(paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(payment));

        mockMvc.perform(get("/api/payment-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderCode").value("PAY123"));
    }

    @Test
    void testGetPaymentByOrderCode_Success() throws Exception {
        String orderCode = "PAY12345";
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderCode(orderCode)
                .amount(BigDecimal.valueOf(100000))
                .status(PaymentStatus.PENDING)
                .build();

        Mockito.when(paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(payment));

        mockMvc.perform(get("/api/payments/{orderCode}", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderCode").value(orderCode));
    }

    @Test
    void testGetPaymentByOrderCode_NotFound() throws Exception {
        String orderCode = "PAY12345";
        Mockito.when(paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/payments/{orderCode}", orderCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testSePayWebhook_Success() throws Exception {
        String orderCode = "PAY12345";
        Payment pendingPayment = Payment.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderCode(orderCode)
                .planId(planId)
                .planName("Pro")
                .cycle("monthly")
                .amount(BigDecimal.valueOf(100000))
                .status(PaymentStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();

        Subscription mockSub = Subscription.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .planId(planId)
                .planName("Pro")
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Mockito.when(paymentRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(pendingPayment));
        Mockito.when(subscriptionRepository.save(any(Subscription.class))).thenReturn(mockSub);
        Mockito.when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        SePayWebhookRequest webhookRequest = SePayWebhookRequest.builder()
                .id(92704L)
                .gateway("Vietcombank")
                .transactionDate("2026-06-18 20:24:05")
                .accountNumber("1234567890")
                .code(orderCode)
                .content(orderCode + " chuyen tien")
                .transferAmount(BigDecimal.valueOf(100000))
                .referenceCode("FT24012345678")
                .build();

        // Retrieve properties setup
        String apiKey = sePayProperties.getApiKey();

        mockMvc.perform(post("/api/payments/webhook/sepay")
                        .header("Authorization", "Apikey " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
