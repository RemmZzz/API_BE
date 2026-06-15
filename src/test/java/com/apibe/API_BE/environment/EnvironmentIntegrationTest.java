package com.apibe.API_BE.environment;

import com.apibe.API_BE.module.environment.entity.*;
import com.apibe.API_BE.module.environment.repository.*;
import com.apibe.API_BE.module.admin.repository.*;
import com.apibe.API_BE.module.collection.repository.*;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.user.repository.*;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;

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

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class EnvironmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Mocks for Context Load without db
    @MockitoBean private AdminUserRepository adminUserRepository;
    @MockitoBean private AdminProjectRepository adminProjectRepository;
    @MockitoBean private AdminPaymentRepository adminPaymentRepository;
    @MockitoBean private AdminApiTestHistoryRepository adminApiTestHistoryRepository;
    @MockitoBean private AdminAiConversationRepository adminAiConversationRepository;
    @MockitoBean private AdminActivityLogRepository adminActivityLogRepository;
    @MockitoBean private AdminAuditLogRepository adminAuditLogRepository;
    @MockitoBean private ApiCollectionRepository apiCollectionRepository;
    @MockitoBean private ApiFolderRepository apiFolderRepository;
    @MockitoBean private ApiRequestRepository apiRequestRepository;
    @MockitoBean private ActiveEnvironmentRepository activeEnvironmentRepository;
    @MockitoBean private EnvironmentRepository environmentRepository;
    @MockitoBean private EnvironmentVariableRepository environmentVariableRepository;
    @MockitoBean private ProjectMemberRepository projectMemberRepository;
    @MockitoBean private ProjectRepository projectRepository;
    @MockitoBean private OtpVerificationRepository otpVerificationRepository;
    @MockitoBean private PasswordResetTokenRepository passwordResetTokenRepository;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private UserSessionRepository userSessionRepository;
    @MockitoBean private UserSettingRepository userSettingRepository;
    @MockitoBean private WorkspaceRepository workspaceRepository;
    @MockitoBean private JdbcTemplate jdbcTemplate;

    @MockitoBean private DatabaseSchemaRepository databaseSchemaRepository;
    @MockitoBean private DatabaseTableRepository databaseTableRepository;
    @MockitoBean private DatabaseColumnRepository databaseColumnRepository;
    @MockitoBean private DatabaseRelationshipRepository databaseRelationshipRepository;
    @MockitoBean private DatabaseIndexRepository databaseIndexRepository;

    private UUID projectId;
    private UUID envId;
    private UUID varId;
    private Environment environment;
    private EnvironmentVariable variable;
    private ActiveEnvironment activeEnvironment;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        envId = UUID.randomUUID();
        varId = UUID.randomUUID();

        environment = Environment.builder()
                .id(envId)
                .projectId(projectId)
                .name("Production")
                .description("Production environment")
                .build();

        variable = EnvironmentVariable.builder()
                .id(varId)
                .environmentId(envId)
                .variableKey("baseUrl")
                .initialValue("https://api.prod.com")
                .currentValue("https://api.prod.com")
                .type("text")
                .isEnabled(true)
                .isSecret(false)
                .build();

        activeEnvironment = ActiveEnvironment.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .environmentId(envId)
                .build();

        Mockito.when(environmentRepository.save(any(Environment.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(environmentVariableRepository.save(any(EnvironmentVariable.class))).thenAnswer(inv -> inv.getArgument(0));
        Mockito.when(activeEnvironmentRepository.save(any(ActiveEnvironment.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void testGetEnvironments() throws Exception {
        Mockito.when(environmentRepository.findByProjectId(projectId))
                .thenReturn(Collections.singletonList(environment));
        Mockito.when(environmentVariableRepository.findByEnvironmentId(envId))
                .thenReturn(Collections.singletonList(variable));

        mockMvc.perform(get("/api/projects/{projectId}/environments", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(envId.toString()))
                .andExpect(jsonPath("$[0].name").value("Production"))
                .andExpect(jsonPath("$[0].variables[0].key").value("baseUrl"));
    }

    @Test
    void testCreateEnvironment() throws Exception {
        String body = """
                {
                    "name": "Staging",
                    "description": "Staging environment"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/environments", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Staging"));
    }

    @Test
    void testUpdateEnvironment() throws Exception {
        Mockito.when(environmentRepository.findById(envId)).thenReturn(Optional.of(environment));

        String body = """
                {
                    "name": "Production Updated",
                    "description": "New description"
                }
                """;

        mockMvc.perform(patch("/api/environments/{environmentId}", envId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Production Updated"));
    }

    @Test
    void testDeleteEnvironment() throws Exception {
        Mockito.when(environmentRepository.findById(envId)).thenReturn(Optional.of(environment));

        mockMvc.perform(delete("/api/environments/{environmentId}", envId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testSetActiveEnvironment() throws Exception {
        Mockito.when(environmentRepository.findById(envId)).thenReturn(Optional.of(environment));
        Mockito.when(activeEnvironmentRepository.findByProjectId(projectId)).thenReturn(Optional.of(activeEnvironment));

        mockMvc.perform(put("/api/projects/{projectId}/active-environment", projectId)
                        .param("environmentId", envId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environmentId").value(envId.toString()));
    }

    @Test
    void testAddVariable() throws Exception {
        Mockito.when(environmentRepository.existsById(envId)).thenReturn(true);

        String body = """
                {
                    "key": "apiKey",
                    "initialValue": "secret-123",
                    "currentValue": "secret-123",
                    "type": "secret",
                    "isEnabled": true,
                    "isSecret": true
                }
                """;

        mockMvc.perform(post("/api/environments/{environmentId}/variables", envId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("apiKey"))
                .andExpect(jsonPath("$.secret").value(true));
    }

    @Test
    void testUpdateVariable() throws Exception {
        Mockito.when(environmentVariableRepository.findById(varId)).thenReturn(Optional.of(variable));

        String body = """
                {
                    "key": "baseUrlUpdated",
                    "currentValue": "https://api-updated.prod.com"
                }
                """;

        mockMvc.perform(patch("/api/environments/variables/{variableId}", varId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("baseUrlUpdated"))
                .andExpect(jsonPath("$.currentValue").value("https://api-updated.prod.com"));
    }

    @Test
    void testDeleteVariable() throws Exception {
        Mockito.when(environmentVariableRepository.existsById(varId)).thenReturn(true);

        mockMvc.perform(delete("/api/environments/variables/{variableId}", varId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testResolveVariables() throws Exception {
        Mockito.when(activeEnvironmentRepository.findByProjectId(projectId)).thenReturn(Optional.of(activeEnvironment));
        Mockito.when(environmentVariableRepository.findByEnvironmentId(envId)).thenReturn(Collections.singletonList(variable));

        String body = """
                {
                    "text": "Call {{baseUrl}}/users with some {{token}}"
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/environments/resolve", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedText").value("Call https://api.prod.com/users with some {{token}}"));
    }
}
