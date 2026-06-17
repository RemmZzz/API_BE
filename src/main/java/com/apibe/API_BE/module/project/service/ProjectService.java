package com.apibe.API_BE.module.project.service;

import com.apibe.API_BE.global.enums.MemberRole;
import com.apibe.API_BE.global.enums.ProjectStatus;
import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.response.PageResponse;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.module.collection.entity.ApiCollection;
import com.apibe.API_BE.module.collection.entity.ApiFolder;
import com.apibe.API_BE.module.collection.entity.ApiRequest;
import com.apibe.API_BE.module.collection.repository.ApiCollectionRepository;
import com.apibe.API_BE.module.collection.repository.ApiFolderRepository;
import com.apibe.API_BE.module.collection.repository.ApiRequestRepository;
import com.apibe.API_BE.module.database.entity.*;
import com.apibe.API_BE.module.database.repository.*;
import com.apibe.API_BE.module.environment.entity.ActiveEnvironment;
import com.apibe.API_BE.module.environment.entity.Environment;
import com.apibe.API_BE.module.environment.entity.EnvironmentVariable;
import com.apibe.API_BE.module.environment.repository.ActiveEnvironmentRepository;
import com.apibe.API_BE.module.environment.repository.EnvironmentRepository;
import com.apibe.API_BE.module.environment.repository.EnvironmentVariableRepository;
import com.apibe.API_BE.module.project.dto.request.CreateProjectRequest;
import com.apibe.API_BE.module.project.dto.request.UpdateProjectRequest;
import com.apibe.API_BE.module.project.dto.response.ProjectDetailResponse;
import com.apibe.API_BE.module.project.dto.response.ProjectResponse;
import com.apibe.API_BE.module.project.entity.Project;
import com.apibe.API_BE.module.project.entity.ProjectMember;
import com.apibe.API_BE.module.project.repository.ProjectMemberRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.workspace.entity.Workspace;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final DatabaseSchemaRepository databaseSchemaRepository;
    private final DatabaseTableRepository databaseTableRepository;
    private final DatabaseColumnRepository databaseColumnRepository;
    private final DatabaseRelationshipRepository databaseRelationshipRepository;
    private final DatabaseIndexRepository databaseIndexRepository;
    private final EnvironmentRepository environmentRepository;
    private final EnvironmentVariableRepository environmentVariableRepository;
    private final ActiveEnvironmentRepository activeEnvironmentRepository;
    private final ApiCollectionRepository apiCollectionRepository;
    private final ApiFolderRepository apiFolderRepository;
    private final ApiRequestRepository apiRequestRepository;
    private final JdbcTemplate jdbcTemplate;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          WorkspaceRepository workspaceRepository,
                          DatabaseSchemaRepository databaseSchemaRepository,
                          DatabaseTableRepository databaseTableRepository,
                          DatabaseColumnRepository databaseColumnRepository,
                          DatabaseRelationshipRepository databaseRelationshipRepository,
                          DatabaseIndexRepository databaseIndexRepository,
                          EnvironmentRepository environmentRepository,
                          EnvironmentVariableRepository environmentVariableRepository,
                          ActiveEnvironmentRepository activeEnvironmentRepository,
                          ApiCollectionRepository apiCollectionRepository,
                          ApiFolderRepository apiFolderRepository,
                          ApiRequestRepository apiRequestRepository,
                          JdbcTemplate jdbcTemplate) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.databaseSchemaRepository = databaseSchemaRepository;
        this.databaseTableRepository = databaseTableRepository;
        this.databaseColumnRepository = databaseColumnRepository;
        this.databaseRelationshipRepository = databaseRelationshipRepository;
        this.databaseIndexRepository = databaseIndexRepository;
        this.environmentRepository = environmentRepository;
        this.environmentVariableRepository = environmentVariableRepository;
        this.activeEnvironmentRepository = activeEnvironmentRepository;
        this.apiCollectionRepository = apiCollectionRepository;
        this.apiFolderRepository = apiFolderRepository;
        this.apiRequestRepository = apiRequestRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<ProjectResponse> getProjects(String keyword, String status, String type, int page, int limit) {
        UUID userId = SecurityUtils.getCurrentUserId();
        int safePage = Math.max(page, 0);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        ProjectStatus parsedStatus = parseProjectStatus(status);
        Page<Project> projects = projectRepository.findByUserAccess(
                userId,
                normalizeFilter(keyword),
                parsedStatus,
                normalizeFilter(type),
                PageRequest.of(safePage, safeLimit, Sort.by(Sort.Direction.DESC, "updatedAt")));

        List<ProjectResponse> items = projects.getContent().stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProjectResponse>builder()
                .items(items)
                .page(projects.getNumber())
                .size(projects.getSize())
                .totalItems(projects.getTotalElements())
                .totalPages(projects.getTotalPages())
                .hasNext(projects.hasNext())
                .hasPrevious(projects.hasPrevious())
                .build();
    }

    public ProjectDetailResponse getProjectById(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Project project = verifyAccessAndGetProject(id, userId);

        UUID workspaceId = workspaceRepository.findByProjectId(project.getId())
                .map(Workspace::getId)
                .orElse(null);

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .type(project.getType())
                .status(project.getStatus())
                .tags(getTags(project.getType()))
                .tech(getTags(project.getType()))
                .apiCount(getApiCount(project.getId()))
                .databaseTableCount(getDatabaseTableCount(project.getId()))
                .aiChatCount(getAiChatCount(project.getId()))
                .color(project.getColor())
                .ownerId(project.getOwnerId())
                .workspaceId(workspaceId)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Project project = Project.builder()
                .ownerId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType() : "API")
                .color(request.getColor() != null ? request.getColor() : "from-indigo-500 to-violet-500")
                .status(ProjectStatus.ACTIVE)
                .build();

        project = projectRepository.save(project);

        // 1. Create OWNER member entry
        ProjectMember ownerMember = ProjectMember.builder()
                .projectId(project.getId())
                .userId(userId)
                .role(MemberRole.OWNER)
                .build();
        projectMemberRepository.save(ownerMember);

        // 2. Create Workspace
        Workspace workspace = Workspace.builder()
                .projectId(project.getId())
                .name(project.getName() + " Workspace")
                .configJson("{}")
                .build();
        workspaceRepository.save(workspace);

        // 3. Create Default Database Schema
        DatabaseSchema schema = DatabaseSchema.builder()
                .projectId(project.getId())
                .dbType("mysql")
                .name("default_schema")
                .build();
        databaseSchemaRepository.save(schema);

        // 4. Create 3 Environments (Dev, Staging, Prod)
        Environment devEnv = Environment.builder()
                .projectId(project.getId())
                .name("Development")
                .description("Môi trường phát triển")
                .build();
        devEnv = environmentRepository.save(devEnv);

        Environment stagingEnv = Environment.builder()
                .projectId(project.getId())
                .name("Staging")
                .description("Môi trường kiểm thử")
                .build();
        environmentRepository.save(stagingEnv);

        Environment prodEnv = Environment.builder()
                .projectId(project.getId())
                .name("Production")
                .description("Môi trường vận hành")
                .build();
        environmentRepository.save(prodEnv);

        // 5. Set default Active Environment to Development
        ActiveEnvironment activeEnv = ActiveEnvironment.builder()
                .projectId(project.getId())
                .environmentId(devEnv.getId())
                .build();
        activeEnvironmentRepository.save(activeEnv);

        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Project project = verifyAccessAndGetProject(id, userId);
        verifyProjectOwnerOrAdmin(project, userId);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            project.setColor(request.getColor());
        }
        if (request.getStatus() != null) {
            project.setStatus(parseProjectStatus(request.getStatus()));
        }

        project = projectRepository.saveAndFlush(project);
        return toProjectResponse(project);
    }

    @Transactional
    public void deleteProject(UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Project project = verifyAccessAndGetProject(id, userId);
        verifyProjectOwnerOrAdmin(project, userId);

        // Soft delete project
        project.setStatus(ProjectStatus.DELETED);
        projectRepository.saveAndFlush(project);
    }

    @Transactional
    public ProjectResponse duplicateProject(UUID projectId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        Project source = verifyAccessAndGetProject(projectId, userId);

        // 1. Clone Project
        Project clone = Project.builder()
                .ownerId(userId)
                .name(source.getName() + " Copy")
                .description(source.getDescription())
                .type(source.getType())
                .color(source.getColor())
                .status(source.getStatus())
                .build();
        clone = projectRepository.save(clone);

        // 2. Clone Project Member (OWNER role for the duplicator)
        ProjectMember ownerMember = ProjectMember.builder()
                .projectId(clone.getId())
                .userId(userId)
                .role(MemberRole.OWNER)
                .build();
        projectMemberRepository.save(ownerMember);

        // 3. Clone Workspace
        Workspace sourceWorkspace = workspaceRepository.findByProjectId(projectId).orElse(null);
        String configJson = sourceWorkspace != null ? sourceWorkspace.getConfigJson() : "{}";
        Workspace workspace = Workspace.builder()
                .projectId(clone.getId())
                .name(clone.getName() + " Workspace")
                .configJson(configJson)
                .build();
        workspaceRepository.save(workspace);

        // 4. Clone Environments and Variables
        List<Environment> sourceEnvs = environmentRepository.findByProjectId(projectId);
        Map<UUID, UUID> envIdMap = new HashMap<>();
        for (Environment env : sourceEnvs) {
            Environment clonedEnv = Environment.builder()
                    .projectId(clone.getId())
                    .name(env.getName())
                    .description(env.getDescription())
                    .build();
            clonedEnv = environmentRepository.save(clonedEnv);
            envIdMap.put(env.getId(), clonedEnv.getId());

            // Clone variables
            List<EnvironmentVariable> vars = environmentVariableRepository.findByEnvironmentId(env.getId());
            for (EnvironmentVariable var : vars) {
                EnvironmentVariable clonedVar = EnvironmentVariable.builder()
                        .environmentId(clonedEnv.getId())
                        .variableKey(var.getVariableKey())
                        .initialValue(var.isSecret() ? null : var.getInitialValue())
                        .currentValue(var.isSecret() ? null : var.getCurrentValue())
                        .type(var.getType())
                        .isEnabled(var.isEnabled())
                        .isSecret(var.isSecret())
                        .build();
                environmentVariableRepository.save(clonedVar);
            }
        }

        // Map Active Environment
        Optional<ActiveEnvironment> activeOpt = activeEnvironmentRepository.findByProjectId(projectId);
        if (activeOpt.isPresent()) {
            UUID targetEnvId = envIdMap.get(activeOpt.get().getEnvironmentId());
            ActiveEnvironment clonedActive = ActiveEnvironment.builder()
                    .projectId(clone.getId())
                    .environmentId(targetEnvId)
                    .build();
            activeEnvironmentRepository.save(clonedActive);
        }

        // 5. Clone Database Schemas, Tables, Columns, Relationships, Indexes
        List<DatabaseSchema> sourceSchemas = databaseSchemaRepository.findByProjectId(projectId).stream().toList();
        for (DatabaseSchema schema : sourceSchemas) {
            DatabaseSchema clonedSchema = DatabaseSchema.builder()
                    .projectId(clone.getId())
                    .dbType(schema.getDbType())
                    .name(schema.getName())
                    .build();
            clonedSchema = databaseSchemaRepository.save(clonedSchema);

            // Clone Tables
            List<DatabaseTable> tables = databaseTableRepository.findBySchemaId(schema.getId());
            Map<UUID, UUID> tableIdMap = new HashMap<>();
            Map<UUID, UUID> columnIdMap = new HashMap<>();

            for (DatabaseTable table : tables) {
                DatabaseTable clonedTable = DatabaseTable.builder()
                        .schemaId(clonedSchema.getId())
                        .name(table.getName())
                        .displayName(table.getDisplayName())
                        .rowCount(table.getRowCount())
                        .positionX(table.getPositionX())
                        .positionY(table.getPositionY())
                        .build();
                clonedTable = databaseTableRepository.save(clonedTable);
                tableIdMap.put(table.getId(), clonedTable.getId());

                // Clone Columns
                List<DatabaseColumn> columns = databaseColumnRepository.findByTableId(table.getId());
                for (DatabaseColumn col : columns) {
                    DatabaseColumn clonedCol = DatabaseColumn.builder()
                            .tableId(clonedTable.getId())
                            .name(col.getName())
                            .dataType(col.getDataType())
                            .length(col.getLength())
                            .precisionValue(col.getPrecisionValue())
                            .scaleValue(col.getScaleValue())
                            .isPrimaryKey(col.isPrimaryKey())
                            .isNullable(col.isNullable())
                            .isUnique(col.isUnique())
                            .isAutoIncrement(col.isAutoIncrement())
                            .defaultValue(col.getDefaultValue())
                            .ordinalPosition(col.getOrdinalPosition())
                            .comment(col.getComment())
                            .build();
                    clonedCol = databaseColumnRepository.save(clonedCol);
                    columnIdMap.put(col.getId(), clonedCol.getId());
                }

                // Clone Indexes
                List<DatabaseIndex> indexes = databaseIndexRepository.findByTableId(table.getId());
                for (DatabaseIndex idx : indexes) {
                    DatabaseIndex clonedIdx = DatabaseIndex.builder()
                            .tableId(clonedTable.getId())
                            .name(idx.getName())
                            .columnsJson(idx.getColumnsJson())
                            .isUnique(idx.isUnique())
                            .indexType(idx.getIndexType())
                            .build();
                    databaseIndexRepository.save(clonedIdx);
                }
            }

            // Clone Relationships
            List<DatabaseRelationship> relationships = databaseRelationshipRepository.findBySchemaId(schema.getId());
            for (DatabaseRelationship rel : relationships) {
                UUID srcTable = tableIdMap.get(rel.getSourceTableId());
                UUID srcCol = columnIdMap.get(rel.getSourceColumnId());
                UUID targetTable = tableIdMap.get(rel.getTargetTableId());
                UUID targetCol = columnIdMap.get(rel.getTargetColumnId());

                if (srcTable != null && srcCol != null && targetTable != null && targetCol != null) {
                    DatabaseRelationship clonedRel = DatabaseRelationship.builder()
                            .schemaId(clonedSchema.getId())
                            .sourceTableId(srcTable)
                            .sourceColumnId(srcCol)
                            .targetTableId(targetTable)
                            .targetColumnId(targetCol)
                            .constraintName(rel.getConstraintName())
                            .onDeleteAction(rel.getOnDeleteAction())
                            .onUpdateAction(rel.getOnUpdateAction())
                            .build();
                    databaseRelationshipRepository.save(clonedRel);
                }
            }
        }

        // 6. Clone API Collections, Folders, Requests
        List<ApiCollection> collections = apiCollectionRepository.findByProjectId(projectId);
        for (ApiCollection col : collections) {
            ApiCollection clonedCol = ApiCollection.builder()
                    .projectId(clone.getId())
                    .name(col.getName())
                    .description(col.getDescription())
                    .build();
            clonedCol = apiCollectionRepository.save(clonedCol);

            // Clone Folders
            List<ApiFolder> folders = apiFolderRepository.findByCollectionId(col.getId());
            Map<UUID, UUID> folderIdMap = new HashMap<>();
            
            // First pass: save all folders, keeping track of parents
            List<ApiFolder> remainingFolders = new ArrayList<>(folders);
            int previousSize = -1;
            while (!remainingFolders.isEmpty() && remainingFolders.size() != previousSize) {
                previousSize = remainingFolders.size();
                Iterator<ApiFolder> iterator = remainingFolders.iterator();
                while (iterator.hasNext()) {
                    ApiFolder folder = iterator.next();
                    if (folder.getParentFolderId() == null || folderIdMap.containsKey(folder.getParentFolderId())) {
                        UUID mappedParentId = folder.getParentFolderId() != null ? folderIdMap.get(folder.getParentFolderId()) : null;
                        ApiFolder clonedFolder = ApiFolder.builder()
                                .collectionId(clonedCol.getId())
                                .parentFolderId(mappedParentId)
                                .name(folder.getName())
                                .ordinalPosition(folder.getOrdinalPosition())
                                .build();
                        clonedFolder = apiFolderRepository.save(clonedFolder);
                        folderIdMap.put(folder.getId(), clonedFolder.getId());
                        iterator.remove();
                    }
                }
            }

            // Cloned requests under collection
            List<ApiRequest> requests = apiRequestRepository.findByCollectionId(col.getId());
            for (ApiRequest req : requests) {
                UUID mappedFolderId = req.getFolderId() != null ? folderIdMap.get(req.getFolderId()) : null;
                ApiRequest clonedReq = ApiRequest.builder()
                        .collectionId(clonedCol.getId())
                        .folderId(mappedFolderId)
                        .name(req.getName())
                        .method(req.getMethod())
                        .url(req.getUrl())
                        .description(req.getDescription())
                        .headers(req.getHeaders())        // <--- ĐÃ SỬA THÀNH headers
                        .params(req.getParams())          // <--- ĐÃ SỬA THÀNH params
                        .body(req.getBody())
                        .bodyType(req.getBodyType())
                        .responseExample(req.getResponseExample())
                        .sortOrder(req.getSortOrder())    // <--- ĐÃ SỬA THÀNH sortOrder
                        .build();
                apiRequestRepository.save(clonedReq);
            }
        }

        return toProjectResponse(clone);
    }

    private ProjectStatus parseProjectStatus(String status) {
        String normalized = normalizeFilter(status);
        if (normalized == null) {
            return null;
        }
        try {
            return ProjectStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Trạng thái dự án không hợp lệ");
        }
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Project verifyAccessAndGetProject(UUID projectId, UUID userId) {
        return projectRepository.findByIdAndUserAccess(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN_ACTION, "Bạn không có quyền truy cập dự án này"));
    }

    private void verifyProjectOwnerOrAdmin(Project project, UUID userId) {
        if (project.getOwnerId().equals(userId)) {
            return;
        }
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(project.getId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN_ACTION, "Bạn không có quyền quản lý dự án này"));
        if (member.getRole() != MemberRole.OWNER && member.getRole() != MemberRole.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION, "Chỉ có chủ sở hữu hoặc quản trị viên dự án mới có quyền này");
        }
    }

    private int getApiCount(UUID projectId) {
        String sql = "SELECT COUNT(r.id) FROM api_requests r " +
                "JOIN api_collections c ON r.collection_id = c.id " +
                "WHERE c.project_id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId.toString());
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getDatabaseTableCount(UUID projectId) {
        String sql = "SELECT COUNT(t.id) FROM database_tables t " +
                "JOIN database_schemas s ON t.schema_id = s.id " +
                "WHERE s.project_id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId.toString());
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getAiChatCount(UUID projectId) {
        String sql = "SELECT COUNT(c.id) FROM ai_conversations c " +
                "WHERE c.project_id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, projectId.toString());
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private List<String> getTags(String type) {
        if (type == null || type.isBlank()) {
            return List.of("API");
        }
        return List.of(type);
    }

    private ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .type(project.getType())
                .status(project.getStatus())
                .tags(getTags(project.getType()))
                .tech(getTags(project.getType()))
                .apiCount(getApiCount(project.getId()))
                .databaseTableCount(getDatabaseTableCount(project.getId()))
                .aiChatCount(getAiChatCount(project.getId()))
                .color(project.getColor())
                .ownerId(project.getOwnerId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}