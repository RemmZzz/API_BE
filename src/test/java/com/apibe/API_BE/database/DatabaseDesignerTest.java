package com.apibe.API_BE.database;

import com.apibe.API_BE.module.database.entity.DatabaseColumn;
import com.apibe.API_BE.module.database.entity.DatabaseRelationship;
import com.apibe.API_BE.module.database.entity.DatabaseSchema;
import com.apibe.API_BE.module.database.entity.DatabaseTable;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;
import com.apibe.API_BE.module.user.repository.UserRepository;
import com.apibe.API_BE.module.user.repository.UserSessionRepository;
import com.apibe.API_BE.module.user.repository.UserSettingRepository;
import com.apibe.API_BE.module.user.repository.OtpVerificationRepository;
import com.apibe.API_BE.module.user.repository.PasswordResetTokenRepository;
import com.apibe.API_BE.module.user.repository.Oauth2ExchangeCodeRepository;
import com.apibe.API_BE.module.admin.repository.*;
import com.apibe.API_BE.module.collection.repository.*;
import com.apibe.API_BE.module.environment.repository.*;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
public class DatabaseDesignerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private com.apibe.API_BE.module.documentation.repository.ApiDocumentationRepository apiDocumentationRepository;
    @MockitoBean private com.apibe.API_BE.module.documentation.repository.ApiDocumentationEndpointRepository apiDocumentationEndpointRepository;

    // Mock all repositories required for context configuration to load without db
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
    @MockitoBean private ProjectRepository projectRepository;
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

    private UUID projectId;
    private UUID schemaId;
    private UUID tableId;
    private UUID columnId;
    private DatabaseSchema schema;
    private DatabaseTable table;
    private DatabaseColumn column;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        schemaId = UUID.randomUUID();
        tableId = UUID.randomUUID();
        columnId = UUID.randomUUID();

        schema = DatabaseSchema.builder()
                .id(schemaId)
                .projectId(projectId)
                .name("test_schema")
                .dbType("mysql")
                .build();

        table = DatabaseTable.builder()
                .id(tableId)
                .schemaId(schemaId)
                .name("users")
                .displayName("Users Table")
                .positionX(100)
                .positionY(150)
                .rowCount(0)
                .build();

        column = DatabaseColumn.builder()
                .id(columnId)
                .tableId(tableId)
                .name("id")
                .dataType("VARCHAR")
                .length(255)
                .isPrimaryKey(true)
                .isNullable(false)
                .isUnique(true)
                .build();

        // Setup common stubbing to avoid NPE in getSchemaByProjectId
        Mockito.when(databaseSchemaRepository.findByProjectId(any(UUID.class))).thenReturn(Optional.of(schema));
        Mockito.when(databaseSchemaRepository.findById(any(UUID.class))).thenReturn(Optional.of(schema));
        Mockito.when(databaseSchemaRepository.save(any(DatabaseSchema.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Mockito.when(databaseTableRepository.save(any(DatabaseTable.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(databaseColumnRepository.save(any(DatabaseColumn.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testGetSchema() throws Exception {
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId)).thenReturn(Collections.singletonList(table));
        Mockito.when(databaseColumnRepository.findByTableIdIn(anyList())).thenReturn(Collections.singletonList(column));
        Mockito.when(databaseRelationshipRepository.findBySchemaId(schemaId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/projects/{projectId}/database-schema", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(schemaId.toString()))
                .andExpect(jsonPath("$.tables[0].name").value("users"))
                .andExpect(jsonPath("$.tables[0].columns[0].name").value("id"))
                .andExpect(jsonPath("$.tables[0].columns[0].type").value("VARCHAR(255)"));
    }

    @Test
    void testCreateTable() throws Exception {
        // We want findBySchemaId to return empty first (when checking duplicates) and then return the table
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId))
                .thenReturn(new ArrayList<>()) // duplicate check returns empty
                .thenReturn(Collections.singletonList(table)); // follow-up load returns table
                
        Mockito.when(databaseColumnRepository.findByTableId(any(UUID.class))).thenReturn(Collections.singletonList(column));

        String requestBody = """
                {
                    "name": "users",
                    "positionX": 100,
                    "positionY": 150,
                    "columns": [
                        {
                            "name": "id",
                            "type": "VARCHAR(255)",
                            "primaryKey": true,
                            "nullable": false,
                            "unique": true
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/projects/{projectId}/database-schema/tables", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables[0].name").value("users"));
    }

    @Test
    void testUpdateTable() throws Exception {
        Mockito.when(databaseTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId)).thenReturn(Collections.singletonList(table));
        Mockito.when(databaseColumnRepository.findByTableId(tableId)).thenReturn(Collections.singletonList(column));

        String requestBody = """
                {
                    "name": "users_updated",
                    "positionX": 200,
                    "positionY": 250
                }
                """;

        mockMvc.perform(patch("/api/database/tables/{tableId}", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables[0].name").value("users_updated"));
    }

    @Test
    void testDeleteTable() throws Exception {
        Mockito.when(databaseTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId)).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/api/database/tables/{tableId}", tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables").isEmpty());
    }

    @Test
    void testAddColumn() throws Exception {
        Mockito.when(databaseTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        Mockito.when(databaseColumnRepository.findByTableId(tableId)).thenReturn(new ArrayList<>());
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId)).thenReturn(Collections.singletonList(table));

        String requestBody = """
                {
                    "name": "email",
                    "type": "VARCHAR(100)",
                    "primaryKey": false,
                    "nullable": true,
                    "unique": false
                }
                """;

        mockMvc.perform(post("/api/database/tables/{tableId}/columns", tableId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateColumn() throws Exception {
        Mockito.when(databaseColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        Mockito.when(databaseTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId)).thenReturn(Collections.singletonList(table));
        Mockito.when(databaseColumnRepository.findByTableId(tableId)).thenReturn(Collections.singletonList(column));

        String requestBody = """
                {
                    "name": "id_updated",
                    "type": "VARCHAR(256)",
                    "primaryKey": true
                }
                """;

        mockMvc.perform(patch("/api/database/columns/{columnId}", columnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteColumn() throws Exception {
        Mockito.when(databaseColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        Mockito.when(databaseTableRepository.findById(tableId)).thenReturn(Optional.of(table));
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId)).thenReturn(Collections.singletonList(table));
        Mockito.when(databaseColumnRepository.findByTableId(tableId)).thenReturn(Collections.emptyList());

        mockMvc.perform(delete("/api/database/columns/{columnId}", columnId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tables[0].columns").isEmpty());
    }

    @Test
    void testCreateRelationship() throws Exception {
        UUID sourceTableId = UUID.randomUUID();
        UUID sourceColId = UUID.randomUUID();
        UUID targetTableId = UUID.randomUUID();
        UUID targetColId = UUID.randomUUID();

        DatabaseRelationship relationship = DatabaseRelationship.builder()
                .id(UUID.randomUUID())
                .schemaId(schemaId)
                .sourceTableId(sourceTableId)
                .sourceColumnId(sourceColId)
                .targetTableId(targetTableId)
                .targetColumnId(targetColId)
                .constraintName("fk_users_roles")
                .build();

        Mockito.when(databaseRelationshipRepository.save(any(DatabaseRelationship.class))).thenReturn(relationship);

        String requestBody = String.format("""
                {
                    "sourceTableId": "%s",
                    "sourceColumnId": "%s",
                    "targetTableId": "%s",
                    "targetColumnId": "%s",
                    "constraintName": "fk_users_roles"
                }
                """, sourceTableId, sourceColId, targetTableId, targetColId);

        mockMvc.perform(post("/api/projects/{projectId}/database-schema/relationship", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.constraintName").value("fk_users_roles"));
    }

    @Test
    void testGetSqlPreview() throws Exception {
        Mockito.when(databaseTableRepository.findBySchemaId(schemaId)).thenReturn(Collections.singletonList(table));
        Mockito.when(databaseColumnRepository.findByTableId(tableId)).thenReturn(Collections.singletonList(column));

        mockMvc.perform(get("/api/projects/{projectId}/database-schema/sql", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sql").value("CREATE TABLE `users` (\n  `id` VARCHAR(255) PRIMARY KEY NOT NULL\n);"));
    }
}
