package com.apibe.API_BE.mockserver;

import com.apibe.API_BE.global.enums.HttpMethodType;
import com.apibe.API_BE.global.enums.ProjectStatus;
import com.apibe.API_BE.module.admin.repository.*;
import com.apibe.API_BE.module.apitester.repository.ApiTestHistoryRepository;
import com.apibe.API_BE.module.collection.repository.*;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.environment.repository.*;
import com.apibe.API_BE.module.mockserver.dto.request.CreateMockEndpointRequest;
import com.apibe.API_BE.module.mockserver.dto.request.UpdateMockEndpointRequest;
import com.apibe.API_BE.module.mockserver.entity.MockEndpoint;
import com.apibe.API_BE.module.mockserver.repository.MockEndpointRepository;
import com.apibe.API_BE.module.project.entity.Project;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.user.repository.*;
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

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class MockEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private MockEndpointRepository mockEndpointRepository;
    @MockitoBean private ProjectRepository projectRepository;

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
    @MockitoBean private WorkspaceRepository workspaceRepository;
    @MockitoBean private Oauth2ExchangeCodeRepository oauth2ExchangeCodeRepository;
    @MockitoBean private JdbcTemplate jdbcTemplate;

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

    private UUID userId;
    private UUID projectId;
    private Project project;
    private MockEndpoint mockEndpoint;
    private UUID mockEndpointId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        mockEndpointId = UUID.randomUUID();

        // Setup Security context
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.when(authentication.getName()).thenReturn(userId.toString());
        Mockito.when(authentication.getPrincipal()).thenReturn(userId.toString());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .ownerId(userId)
                .status(ProjectStatus.ACTIVE)
                .build();

        mockEndpoint = MockEndpoint.builder()
                .id(mockEndpointId)
                .projectId(projectId)
                .method(HttpMethodType.GET)
                .path("/users/{id}")
                .statusCode(200)
                .delayMs(50)
                .responseHeadersJson("{\"Content-Type\":\"application/json\",\"X-Test\":\"Mocked\"}")
                .responseBody("{\"id\": 5, \"name\": \"Antigravity\"}")
                .isEnabled(true)
                .build();

        Mockito.when(mockEndpointRepository.save(any(MockEndpoint.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMockEndpoints_Success() throws Exception {
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.of(project));
        Mockito.when(mockEndpointRepository.findByProjectId(projectId)).thenReturn(Collections.singletonList(mockEndpoint));

        mockMvc.perform(get("/api/projects/{projectId}/mock-endpoints", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(mockEndpointId.toString()))
                .andExpect(jsonPath("$.data[0].path").value("/users/{id}"));
    }

    @Test
    void testGetMockEndpoints_Forbidden() throws Exception {
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/{projectId}/mock-endpoints", projectId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User does not have access to this project"));
    }

    @Test
    void testCreateMockEndpoint_Success() throws Exception {
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.of(project));
        Mockito.when(mockEndpointRepository.findByProjectIdAndMethodAndPath(projectId, HttpMethodType.POST, "/users"))
                .thenReturn(Optional.empty());

        CreateMockEndpointRequest request = CreateMockEndpointRequest.builder()
                .method(HttpMethodType.POST)
                .path("users") // Unnormalized
                .statusCode(201)
                .delayMs(0)
                .responseBody("{\"created\": true}")
                .build();

        mockMvc.perform(post("/api/projects/{projectId}/mock-endpoints", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.path").value("/users")) // Normalized
                .andExpect(jsonPath("$.data.statusCode").value(201));
    }

    @Test
    void testUpdateMockEndpoint_Success() throws Exception {
        Mockito.when(mockEndpointRepository.findById(mockEndpointId)).thenReturn(Optional.of(mockEndpoint));
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.of(project));

        UpdateMockEndpointRequest request = UpdateMockEndpointRequest.builder()
                .statusCode(202)
                .delayMs(100)
                .build();

        mockMvc.perform(patch("/api/mock-endpoints/{mockEndpointId}", mockEndpointId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statusCode").value(202))
                .andExpect(jsonPath("$.data.delayMs").value(100));
    }

    @Test
    void testDeleteMockEndpoint_Success() throws Exception {
        Mockito.when(mockEndpointRepository.findById(mockEndpointId)).thenReturn(Optional.of(mockEndpoint));
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.of(project));

        mockMvc.perform(delete("/api/mock-endpoints/{mockEndpointId}", mockEndpointId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mock endpoint deleted successfully"));

        Mockito.verify(mockEndpointRepository).delete(mockEndpoint);
    }

    @Test
    void testExecuteMock_ExactMatch_Success() throws Exception {
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(mockEndpointRepository.findByProjectIdAndMethodAndPath(projectId, HttpMethodType.GET, "/users/5"))
                .thenReturn(Optional.of(mockEndpoint));

        mockMvc.perform(get("/mock/{projectId}/users/5", projectId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Test", "Mocked"))
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(content().json("{\"id\": 5, \"name\": \"Antigravity\"}"));
    }

    @Test
    void testExecuteMock_PatternMatch_Success() throws Exception {
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(mockEndpointRepository.findByProjectIdAndMethodAndPath(projectId, HttpMethodType.GET, "/users/10"))
                .thenReturn(Optional.empty());

        Mockito.when(mockEndpointRepository.findByProjectIdAndMethodAndIsEnabledTrue(projectId, HttpMethodType.GET))
                .thenReturn(Collections.singletonList(mockEndpoint)); // path is "/users/{id}"

        mockMvc.perform(get("/mock/{projectId}/users/10", projectId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Test", "Mocked"))
                .andExpect(content().json("{\"id\": 5, \"name\": \"Antigravity\"}"));
    }

    @Test
    void testExecuteMock_NotFound() throws Exception {
        Mockito.when(projectRepository.existsById(projectId)).thenReturn(true);
        Mockito.when(mockEndpointRepository.findByProjectIdAndMethodAndPath(projectId, HttpMethodType.GET, "/unknown"))
                .thenReturn(Optional.empty());
        Mockito.when(mockEndpointRepository.findByProjectIdAndMethodAndIsEnabledTrue(projectId, HttpMethodType.GET))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/mock/{projectId}/unknown", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No matching mock endpoint found"));
    }
}
