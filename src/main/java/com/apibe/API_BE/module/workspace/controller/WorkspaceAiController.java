package com.apibe.API_BE.module.workspace.controller;

import com.apibe.API_BE.module.workspace.dto.request.CreateConversationRequest;
import com.apibe.API_BE.module.workspace.dto.request.SendMessageRequest;
import com.apibe.API_BE.module.workspace.dto.request.UpdateConversationRequest;
import com.apibe.API_BE.module.workspace.entity.AiConversation;
import com.apibe.API_BE.module.workspace.entity.AiMessage;
import com.apibe.API_BE.module.workspace.entity.Workspace;
import com.apibe.API_BE.module.workspace.service.WorkspaceAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkspaceAiController {

    private final WorkspaceAiService workspaceAiService;

    @GetMapping("/projects/{projectId}/workspace")
    public ResponseEntity<Workspace> getWorkspace(@PathVariable UUID projectId) {
        return ResponseEntity.ok(workspaceAiService.getWorkspace(projectId));
    }

    @GetMapping("/projects/{projectId}/conversations")
    public ResponseEntity<List<AiConversation>> getConversations(@PathVariable UUID projectId) {
        return ResponseEntity.ok(workspaceAiService.getConversations(projectId));
    }

    @PostMapping("/projects/{projectId}/conversations")
    public ResponseEntity<AiConversation> createConversation(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateConversationRequest request) {
        return ResponseEntity.ok(workspaceAiService.createConversation(projectId, request.getTitle(), request.getMode()));
    }

    @PatchMapping("/conversations/{conversationId}")
    public ResponseEntity<AiConversation> updateConversation(
            @PathVariable UUID conversationId,
            @RequestBody UpdateConversationRequest request) {
        return ResponseEntity.ok(workspaceAiService.updateConversation(
                conversationId, request.getTitle(), request.getPinned(), request.getArchived()));
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID conversationId) {
        workspaceAiService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<AiMessage>> getMessages(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(workspaceAiService.getMessages(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(workspaceAiService.sendAiMessage(conversationId, request.getContent()));
    }
}
