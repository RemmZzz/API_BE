package com.apibe.API_BE.module.admin.repository;

import com.apibe.API_BE.module.workspace.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminAiConversationRepository extends JpaRepository<AiConversation, UUID> {
}
