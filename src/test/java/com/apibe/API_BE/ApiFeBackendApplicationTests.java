package com.apibe.API_BE;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.apibe.API_BE.module.admin.repository.AdminActivityLogRepository;
import com.apibe.API_BE.module.admin.repository.AdminAiConversationRepository;
import com.apibe.API_BE.module.admin.repository.AdminApiTestHistoryRepository;
import com.apibe.API_BE.module.admin.repository.AdminAuditLogRepository;
import com.apibe.API_BE.module.admin.repository.AdminPaymentRepository;
import com.apibe.API_BE.module.admin.repository.AdminProjectRepository;
import com.apibe.API_BE.module.admin.repository.AdminUserRepository;
import com.apibe.API_BE.module.collection.repository.ApiCollectionRepository;
import com.apibe.API_BE.module.collection.repository.ApiFolderRepository;
import com.apibe.API_BE.module.collection.repository.ApiRequestRepository;
import com.apibe.API_BE.module.collection.repository.CollectionFolderRepository;
import com.apibe.API_BE.module.collection.repository.CollectionRepository;
import com.apibe.API_BE.module.documentation.repository.ApiDocumentationEndpointRepository;
import com.apibe.API_BE.module.database.repository.DatabaseColumnRepository;
import com.apibe.API_BE.module.database.repository.DatabaseIndexRepository;
import com.apibe.API_BE.module.database.repository.DatabaseRelationshipRepository;
import com.apibe.API_BE.module.database.repository.DatabaseSchemaRepository;
import com.apibe.API_BE.module.database.repository.DatabaseTableRepository;
import com.apibe.API_BE.module.environment.repository.ActiveEnvironmentRepository;
import com.apibe.API_BE.module.environment.repository.EnvironmentRepository;
import com.apibe.API_BE.module.environment.repository.EnvironmentVariableRepository;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.user.repository.OtpVerificationRepository;
import com.apibe.API_BE.module.user.repository.PasswordResetTokenRepository;
import com.apibe.API_BE.module.user.repository.UserRepository;
import com.apibe.API_BE.module.user.repository.UserSessionRepository;
import com.apibe.API_BE.module.user.repository.UserSettingRepository;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;
import com.apibe.API_BE.module.apitester.repository.ApiTestHistoryRepository;
import com.apibe.API_BE.module.user.repository.Oauth2ExchangeCodeRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class ApiFeBackendApplicationTests {

    @MockitoBean
    private ApiTestHistoryRepository apiTestHistoryRepository;

    @MockitoBean
    private com.apibe.API_BE.module.documentation.repository.ApiDocumentationRepository apiDocumentationRepository;

    @MockitoBean
    private ApiDocumentationEndpointRepository apiDocumentationEndpointRepository;

    @MockitoBean
    private AdminUserRepository adminUserRepository;

    @MockitoBean
    private AdminProjectRepository adminProjectRepository;

    @MockitoBean
    private AdminPaymentRepository adminPaymentRepository;

    @MockitoBean
    private AdminApiTestHistoryRepository adminApiTestHistoryRepository;

    @MockitoBean
    private AdminAiConversationRepository adminAiConversationRepository;

    @MockitoBean
    private AdminActivityLogRepository adminActivityLogRepository;

    @MockitoBean
    private AdminAuditLogRepository adminAuditLogRepository;

    @MockitoBean
    private ApiCollectionRepository apiCollectionRepository;

    @MockitoBean
    private CollectionRepository collectionRepository;

    @MockitoBean
    private CollectionFolderRepository collectionFolderRepository;

    @MockitoBean
    private ApiFolderRepository apiFolderRepository;

    @MockitoBean
    private ApiRequestRepository apiRequestRepository;

    @MockitoBean
    private DatabaseColumnRepository databaseColumnRepository;

    @MockitoBean
    private DatabaseIndexRepository databaseIndexRepository;

    @MockitoBean
    private DatabaseRelationshipRepository databaseRelationshipRepository;

    @MockitoBean
    private DatabaseSchemaRepository databaseSchemaRepository;

    @MockitoBean
    private DatabaseTableRepository databaseTableRepository;

    @MockitoBean
    private ActiveEnvironmentRepository activeEnvironmentRepository;

    @MockitoBean
    private EnvironmentRepository environmentRepository;

    @MockitoBean
    private EnvironmentVariableRepository environmentVariableRepository;

    @MockitoBean
    private ProjectMemberRepository projectMemberRepository;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private OtpVerificationRepository otpVerificationRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserSessionRepository userSessionRepository;

    @MockitoBean
    private UserSettingRepository userSettingRepository;

    @MockitoBean
    private WorkspaceRepository workspaceRepository;

    @MockitoBean
    private Oauth2ExchangeCodeRepository oauth2ExchangeCodeRepository;

    @MockitoBean
    private com.apibe.API_BE.module.mockserver.repository.MockEndpointRepository mockEndpointRepository;

    @MockitoBean
    private com.apibe.API_BE.module.subscription.repository.SubscriptionPlanRepository subscriptionPlanRepository;

    @MockitoBean
    private com.apibe.API_BE.module.subscription.repository.SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private com.apibe.API_BE.module.payment.repository.PaymentRepository paymentRepository;

    @MockitoBean
    private com.apibe.API_BE.module.payment.repository.PaymentEventRepository paymentEventRepository;

    @MockitoBean
    private com.apibe.API_BE.module.workspace.repository.AiConversationRepository aiConversationRepository;

    @MockitoBean
    private com.apibe.API_BE.module.workspace.repository.AiMessageRepository aiMessageRepository;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }
}

