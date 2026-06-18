package com.apibe.API_BE.workspace;

import com.apibe.API_BE.global.enums.ProjectStatus;
import com.apibe.API_BE.module.admin.repository.*;
import com.apibe.API_BE.module.apitester.repository.ApiTestHistoryRepository;
import com.apibe.API_BE.module.collection.repository.*;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.environment.repository.*;
import com.apibe.API_BE.module.project.entity.Project;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.user.repository.*;
import com.apibe.API_BE.module.mockserver.repository.MockEndpointRepository;
import com.apibe.API_BE.module.workspace.dto.request.CreateConversationRequest;
import com.apibe.API_BE.module.workspace.dto.request.SendMessageRequest;
import com.apibe.API_BE.module.workspace.dto.request.UpdateConversationRequest;
import com.apibe.API_BE.module.workspace.entity.AiConversation;
import com.apibe.API_BE.module.workspace.entity.AiMessage;
import com.apibe.API_BE.module.workspace.entity.Workspace;
import com.apibe.API_BE.module.workspace.repository.AiConversationRepository;
import com.apibe.API_BE.module.workspace.repository.AiMessageRepository;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;
import com.apibe.API_BE.module.workspace.service.GeminiServiceClient;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class WorkspaceAiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private ProjectRepository projectRepository;
    @MockitoBean private WorkspaceRepository workspaceRepository;
    @MockitoBean private AiConversationRepository aiConversationRepository;
    @MockitoBean private AiMessageRepository aiMessageRepository;
    @MockitoBean private MockEndpointRepository mockEndpointRepository;
    @MockitoBean private GeminiServiceClient geminiServiceClient;

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

    @MockitoBean private com.apibe.API_BE.module.subscription.repository.SubscriptionPlanRepository subscriptionPlanRepository;
    @MockitoBean private com.apibe.API_BE.module.subscription.repository.SubscriptionRepository subscriptionRepository;
    @MockitoBean private com.apibe.API_BE.module.payment.repository.PaymentRepository paymentRepository;
    @MockitoBean private com.apibe.API_BE.module.payment.repository.PaymentEventRepository paymentEventRepository;

    @MockitoBean private DatabaseSchemaRepository databaseSchemaRepository;
    @MockitoBean private DatabaseTableRepository databaseTableRepository;
    @MockitoBean private DatabaseColumnRepository databaseColumnRepository;
    @MockitoBean private DatabaseRelationshipRepository databaseRelationshipRepository;
    @MockitoBean private DatabaseIndexRepository databaseIndexRepository;

    private UUID userId;
    private UUID projectId;
    private Project project;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();

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

        Mockito.when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.of(project));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetWorkspace_Success() throws Exception {
        Workspace mockWorkspace = Workspace.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .name("Test Workspace")
                .configJson("{\"defaultMode\":\"chat\"}")
                .build();

        Mockito.when(workspaceRepository.findByProjectId(projectId)).thenReturn(Optional.of(mockWorkspace));

        mockMvc.perform(get("/api/projects/{projectId}/workspace", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Workspace"))
                .andExpect(jsonPath("$.configJson").value("{\"defaultMode\":\"chat\"}"));
    }

    @Test
    void testGetWorkspace_Forbidden() throws Exception {
        Mockito.when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/{projectId}/workspace", projectId))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetConversations_Success() throws Exception {
        AiConversation mockConversation = AiConversation.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .userId(userId)
                .title("Test Chat")
                .mode("chat")
                .pinned(false)
                .archived(false)
                .build();

        Mockito.when(aiConversationRepository.findByProjectIdAndUserIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(projectId, userId))
                .thenReturn(List.of(mockConversation));

        mockMvc.perform(get("/api/projects/{projectId}/conversations", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Chat"))
                .andExpect(jsonPath("$[0].mode").value("chat"));
    }

    @Test
    void testCreateConversation_Success() throws Exception {
        CreateConversationRequest request = new CreateConversationRequest();
        request.setTitle("New API Chat");
        request.setMode("api");

        Mockito.when(aiConversationRepository.save(any(AiConversation.class))).thenAnswer(inv -> {
            AiConversation saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        mockMvc.perform(post("/api/projects/{projectId}/conversations", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New API Chat"))
                .andExpect(jsonPath("$.mode").value("api"));
    }

    @Test
    void testUpdateConversation_Success() throws Exception {
        UUID conversationId = UUID.randomUUID();
        AiConversation existing = AiConversation.builder()
                .id(conversationId)
                .projectId(projectId)
                .userId(userId)
                .title("Old Chat")
                .mode("chat")
                .pinned(false)
                .archived(false)
                .build();

        Mockito.when(aiConversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(existing));
        Mockito.when(aiConversationRepository.save(any(AiConversation.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateConversationRequest request = new UpdateConversationRequest();
        request.setTitle("Renamed Chat");
        request.setPinned(true);

        mockMvc.perform(patch("/api/conversations/{conversationId}", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Renamed Chat"))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void testDeleteConversation_Success() throws Exception {
        UUID conversationId = UUID.randomUUID();
        AiConversation existing = AiConversation.builder()
                .id(conversationId)
                .projectId(projectId)
                .userId(userId)
                .build();

        Mockito.when(aiConversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/api/conversations/{conversationId}", conversationId))
                .andExpect(status().isNoContent());

        Mockito.verify(aiMessageRepository).deleteByConversationId(conversationId);
        Mockito.verify(aiConversationRepository).delete(existing);
    }

    @Test
    void testSendMessage_Success() throws Exception {
        UUID conversationId = UUID.randomUUID();
        AiConversation conversation = AiConversation.builder()
                .id(conversationId)
                .projectId(projectId)
                .userId(userId)
                .mode("chat")
                .build();

        Mockito.when(aiConversationRepository.findByIdAndUserId(conversationId, userId)).thenReturn(Optional.of(conversation));
        Mockito.when(aiMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)).thenReturn(new ArrayList<>());
        Mockito.when(aiMessageRepository.save(any(AiMessage.class))).thenAnswer(inv -> {
            AiMessage saved = inv.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Mock GeminiServiceClient generateContent
        Map<String, Object> geminiResult = new HashMap<>();
        geminiResult.put("content", "This is an AI response.");
        geminiResult.put("metadata", Map.of("testKey", "testValue"));
        Mockito.when(geminiServiceClient.generateContent(eq("chat"), anyList(), eq("Hello AI")))
                .thenReturn(geminiResult);

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("Hello AI");

        mockMvc.perform(post("/api/conversations/{conversationId}/messages", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userMessage.content").value("Hello AI"))
                .andExpect(jsonPath("$.assistantMessage.content").value("This is an AI response."))
                .andExpect(jsonPath("$.assistantMessage.metadataJson").value("{\"testKey\":\"testValue\"}"));
    }
}
