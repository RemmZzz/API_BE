package com.apibe.API_BE.documentation;

import com.apibe.API_BE.module.admin.repository.*;
import com.apibe.API_BE.module.collection.entity.ApiCollection;
import com.apibe.API_BE.module.collection.entity.ApiRequest;
import com.apibe.API_BE.global.enums.HttpMethodType;
import com.apibe.API_BE.module.collection.repository.*;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.documentation.entity.ApiDocumentation;
import com.apibe.API_BE.module.documentation.entity.ApiDocumentationEndpoint;
import com.apibe.API_BE.module.documentation.repository.ApiDocumentationEndpointRepository;
import com.apibe.API_BE.module.documentation.repository.ApiDocumentationRepository;
import com.apibe.API_BE.module.environment.repository.*;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.user.repository.*;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;
import com.apibe.API_BE.module.apitester.repository.ApiTestHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class ApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Required Mocks for Context Load without db
    @MockitoBean private AdminUserRepository adminUserRepository;
    @MockitoBean private AdminProjectRepository adminProjectRepository;
    @MockitoBean private AdminPaymentRepository adminPaymentRepository;
    @MockitoBean private AdminApiTestHistoryRepository adminApiTestHistoryRepository;
    @MockitoBean private AdminAiConversationRepository adminAiConversationRepository;
    @MockitoBean private AdminActivityLogRepository adminActivityLogRepository;
    @MockitoBean private AdminAuditLogRepository adminAuditLogRepository;
    @MockitoBean private ProjectMemberRepository projectMemberRepository;
    @MockitoBean private OtpVerificationRepository otpVerificationRepository;
    @MockitoBean private PasswordResetTokenRepository passwordResetTokenRepository;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private UserSessionRepository userSessionRepository;
    @MockitoBean private UserSettingRepository userSettingRepository;
    @MockitoBean private WorkspaceRepository workspaceRepository;
    @MockitoBean private Oauth2ExchangeCodeRepository oauth2ExchangeCodeRepository;
    @MockitoBean private JdbcTemplate jdbcTemplate;

    @MockitoBean private com.apibe.API_BE.module.mockserver.repository.MockEndpointRepository mockEndpointRepository;
    @MockitoBean private com.apibe.API_BE.module.subscription.repository.SubscriptionPlanRepository subscriptionPlanRepository;
    @MockitoBean private com.apibe.API_BE.module.subscription.repository.SubscriptionRepository subscriptionRepository;
    @MockitoBean private com.apibe.API_BE.module.payment.repository.PaymentRepository paymentRepository;
    @MockitoBean private com.apibe.API_BE.module.payment.repository.PaymentEventRepository paymentEventRepository;

    @MockitoBean private com.apibe.API_BE.module.workspace.repository.AiConversationRepository aiConversationRepository;
    @MockitoBean private com.apibe.API_BE.module.workspace.repository.AiMessageRepository aiMessageRepository;

    @MockitoBean private DatabaseSchemaRepository databaseSchemaRepository;
    @MockitoBean private DatabaseTableRepository databaseTableRepository;
    @MockitoBean private DatabaseColumnRepository databaseColumnRepository;
    @MockitoBean private DatabaseRelationshipRepository databaseRelationshipRepository;
    @MockitoBean private DatabaseIndexRepository databaseIndexRepository;

    @MockitoBean private ActiveEnvironmentRepository activeEnvironmentRepository;
    @MockitoBean private EnvironmentRepository environmentRepository;
    @MockitoBean private EnvironmentVariableRepository environmentVariableRepository;

    // Module-specific Mocks
    @MockitoBean private ProjectRepository projectRepository;
    @MockitoBean private ApiCollectionRepository apiCollectionRepository;
    @MockitoBean private CollectionRepository collectionRepository;
    @MockitoBean private CollectionFolderRepository collectionFolderRepository;
    @MockitoBean private ApiRequestRepository apiRequestRepository;
    @MockitoBean private ApiFolderRepository apiFolderRepository;
    @MockitoBean private ApiDocumentationRepository apiDocumentationRepository;
    @MockitoBean private ApiDocumentationEndpointRepository apiDocumentationEndpointRepository;
    @MockitoBean private ApiTestHistoryRepository apiTestHistoryRepository;

    private UUID projectId;
    private ApiDocumentation mockDocumentation;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();

        mockDocumentation = ApiDocumentation.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .title("Project API Docs")
                .version("1.0.0")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .endpoints(new ArrayList<>())
                .build();

        ApiDocumentationEndpoint endpoint = ApiDocumentationEndpoint.builder()
                .id(UUID.randomUUID())
                .documentation(mockDocumentation)
                .method("GET")
                .url("/api/v1/test")
                .description("Test Endpoint")
                .headersJson("[]")
                .paramsJson("[]")
                .bodyExample("{}")
                .responseExample("{\"ok\":true}")
                .errorExample("{\"ok\":false}")
                .build();
        mockDocumentation.getEndpoints().add(endpoint);

        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(apiDocumentationRepository.save(any(ApiDocumentation.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void testGetDocumentation_NotFound() throws Exception {
        UUID nonExistentProjectId = UUID.randomUUID();
        Mockito.when(projectRepository.existsById(nonExistentProjectId)).thenReturn(false);

        mockMvc.perform(get("/api/projects/{projectId}/documentation", nonExistentProjectId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDocumentation_Default() throws Exception {
        Mockito.when(apiDocumentationRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/{projectId}/documentation", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value("API Documentation"))
                .andExpect(jsonPath("$.endpoints").isEmpty());
    }

    @Test
    void testGetDocumentation_Success() throws Exception {
        Mockito.when(apiDocumentationRepository.findByProjectId(projectId)).thenReturn(Optional.of(mockDocumentation));

        mockMvc.perform(get("/api/projects/{projectId}/documentation", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value("Project API Docs"))
                .andExpect(jsonPath("$.endpoints[0].method").value("GET"))
                .andExpect(jsonPath("$.endpoints[0].url").value("/api/v1/test"));
    }

    @Test
    void testSaveDocumentation_Success() throws Exception {
        Mockito.when(apiDocumentationRepository.findByProjectId(projectId)).thenReturn(Optional.of(mockDocumentation));

        String body = """
                {
                    "title": "Updated Docs Title",
                    "version": "2.0.0",
                    "endpoints": [
                        {
                            "method": "POST",
                            "url": "/api/v1/save",
                            "description": "Save data",
                            "headers": [{"key": "Content-Type", "value": "application/json"}],
                            "params": [],
                            "bodyExample": "{\\"name\\": \\"test\\"}",
                            "responseExample": "{\\"id\\": 1}"
                        }
                    ]
                }
                """;

        mockMvc.perform(put("/api/projects/{projectId}/documentation", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Docs Title"))
                .andExpect(jsonPath("$.version").value("2.0.0"))
                .andExpect(jsonPath("$.endpoints[0].method").value("POST"))
                .andExpect(jsonPath("$.endpoints[0].url").value("/api/v1/save"));
    }

    @Test
    void testGenerateDocumentation_Success() throws Exception {
        Mockito.when(apiDocumentationRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        UUID collectionId = UUID.randomUUID();
        ApiCollection collection = ApiCollection.builder()
                .id(collectionId)
                .projectId(projectId)
                .name("Mock Collection")
                .build();

        ApiRequest request = ApiRequest.builder()
                .id(UUID.randomUUID())
                .collectionId(collectionId)
                .name("Get Users")
                .method(HttpMethodType.GET)
                .url("/api/users")
                .description("Get list of users")
                .headers("[]")
                .params("[]")
                .body("{}")
                .responseExample("[]")
                .build();

        Mockito.when(apiCollectionRepository.findByProjectId(projectId)).thenReturn(Collections.singletonList(collection));
        Mockito.when(apiRequestRepository.findByCollectionId(collectionId)).thenReturn(Collections.singletonList(request));

        mockMvc.perform(post("/api/projects/{projectId}/documentation/generate", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoints[0].method").value("GET"))
                .andExpect(jsonPath("$.endpoints[0].url").value("/api/users"))
                .andExpect(jsonPath("$.endpoints[0].description").value("Get list of users"));
    }

    @Test
    void testExportMarkdown_Success() throws Exception {
        Mockito.when(apiDocumentationRepository.findByProjectId(projectId)).thenReturn(Optional.of(mockDocumentation));

        mockMvc.perform(get("/api/projects/{projectId}/documentation/export.md", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("# Project API Docs")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("## GET /api/v1/test")));
    }
}
