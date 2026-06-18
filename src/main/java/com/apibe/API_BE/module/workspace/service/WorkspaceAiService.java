package com.apibe.API_BE.module.workspace.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.apibe.API_BE.module.workspace.entity.AiConversation;
import com.apibe.API_BE.module.workspace.entity.AiMessage;
import com.apibe.API_BE.module.workspace.entity.Workspace;
import com.apibe.API_BE.module.workspace.repository.AiConversationRepository;
import com.apibe.API_BE.module.workspace.repository.AiMessageRepository;
import com.apibe.API_BE.module.workspace.repository.WorkspaceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"null", "unchecked"})
public class WorkspaceAiService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AiConversationRepository aiConversationRepository;
    private final AiMessageRepository aiMessageRepository;
    private final GeminiServiceClient geminiServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * Checks if the user has permission to access the project.
     */
    private void checkProjectAccess(UUID projectId, UUID userId) {
        projectRepository.findByIdAndUserAccess(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền truy cập dự án này"));
    }

    /**
     * Fetch workspace settings for a project. Automatically create default if not exists.
     */
    @Transactional
    public Workspace getWorkspace(UUID projectId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        checkProjectAccess(projectId, userId);

        return workspaceRepository.findByProjectId(projectId)
                .orElseGet(() -> {
                    Workspace newWorkspace = Workspace.builder()
                            .projectId(projectId)
                            .name("Default Workspace")
                            .configJson("{\"defaultMode\":\"chat\"}")
                            .build();
                    return workspaceRepository.save(newWorkspace);
                });
    }

    /**
     * Fetch all active conversations for a project belonging to current user.
     */
    public List<AiConversation> getConversations(UUID projectId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        checkProjectAccess(projectId, userId);

        return aiConversationRepository.findByProjectIdAndUserIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(projectId, userId);
    }

    /**
     * Create a new AI Conversation.
     */
    @Transactional
    public AiConversation createConversation(UUID projectId, String title, String mode) {
        UUID userId = SecurityUtils.getCurrentUserId();
        checkProjectAccess(projectId, userId);

        AiConversation conversation = AiConversation.builder()
                .projectId(projectId)
                .userId(userId)
                .title(title != null && !title.trim().isEmpty() ? title : "Đoạn chat mới")
                .mode(mode != null && !mode.trim().isEmpty() ? mode : "chat")
                .pinned(false)
                .archived(false)
                .build();

        return aiConversationRepository.save(conversation);
    }

    /**
     * Update an AI Conversation (rename, pin, archive).
     */
    @Transactional
    public AiConversation updateConversation(UUID conversationId, String title, Boolean pinned, Boolean archived) {
        UUID userId = SecurityUtils.getCurrentUserId();
        AiConversation conversation = aiConversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy cuộc hội thoại này"));

        checkProjectAccess(conversation.getProjectId(), userId);

        if (title != null && !title.trim().isEmpty()) {
            conversation.setTitle(title);
        }
        if (pinned != null) {
            conversation.setPinned(pinned);
        }
        if (archived != null) {
            conversation.setArchived(archived);
        }
        conversation.setUpdatedAt(LocalDateTime.now());

        return aiConversationRepository.save(conversation);
    }

    /**
     * Delete an AI Conversation and all its messages.
     */
    @Transactional
    public void deleteConversation(UUID conversationId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        AiConversation conversation = aiConversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy cuộc hội thoại này"));

        checkProjectAccess(conversation.getProjectId(), userId);

        aiMessageRepository.deleteByConversationId(conversationId);
        aiConversationRepository.delete(conversation);
    }

    /**
     * Get message history for a conversation.
     */
    public List<AiMessage> getMessages(UUID conversationId) {
        UUID userId = SecurityUtils.getCurrentUserId();
        AiConversation conversation = aiConversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy cuộc hội thoại này"));

        checkProjectAccess(conversation.getProjectId(), userId);

        return aiMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Send user message to AI and return both user and AI message.
     */
    @Transactional
    public Map<String, Object> sendAiMessage(UUID conversationId, String content) {
        UUID userId = SecurityUtils.getCurrentUserId();
        AiConversation conversation = aiConversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy cuộc hội thoại này"));

        checkProjectAccess(conversation.getProjectId(), userId);

        // 1. Save user message
        AiMessage userMessage = AiMessage.builder()
                .conversationId(conversationId)
                .role("user")
                .content(content)
                .build();
        aiMessageRepository.save(userMessage);

        // 2. Fetch history (limit to last 10 messages for token efficiency and context)
        List<AiMessage> allMessages = aiMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        // Exclude the newly added user message from history, then take last 10
        List<AiMessage> history = allMessages.stream()
                .filter(m -> !m.getId().equals(userMessage.getId()))
                .collect(Collectors.toList());

        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
        }

        List<Map<String, String>> chatHistory = history.stream().map(m -> {
            Map<String, String> map = new HashMap<>();
            map.put("role", m.getRole());
            map.put("content", m.getContent());
            return map;
        }).collect(Collectors.toList());

        // 3. Call Gemini API
        Map<String, Object> aiResult = geminiServiceClient.generateContent(conversation.getMode(), chatHistory, content);
        String replyText = (String) aiResult.get("content");
        Map<String, Object> metadata = (Map<String, Object>) aiResult.get("metadata");

        String metadataJson = null;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (JsonProcessingException e) {
                log.warn("Failed to write metadata as JSON string: ", e);
            }
        }

        // 4. Save AI response message
        AiMessage assistantMessage = AiMessage.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(replyText)
                .metadataJson(metadataJson)
                .build();
        aiMessageRepository.save(assistantMessage);

        // 5. Update conversation updatedAt timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        aiConversationRepository.save(conversation);

        // 6. Return response
        Map<String, Object> response = new HashMap<>();
        response.put("userMessage", userMessage);
        response.put("assistantMessage", assistantMessage);
        return response;
    }
}
