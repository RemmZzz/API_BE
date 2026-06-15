package com.apibe.API_BE.module.admin.repository;

import com.apibe.API_BE.module.documentation.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminAiConversationRepository extends JpaRepository<AiConversation, UUID> {
}
