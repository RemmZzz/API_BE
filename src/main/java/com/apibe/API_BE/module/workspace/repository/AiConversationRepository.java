package com.apibe.API_BE.module.workspace.repository;

import com.apibe.API_BE.module.workspace.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiConversationRepository extends JpaRepository<AiConversation, UUID> {
    List<AiConversation> findByProjectIdAndUserIdAndArchivedFalseOrderByPinnedDescUpdatedAtDesc(UUID projectId, UUID userId);
    List<AiConversation> findByProjectIdAndUserIdOrderByPinnedDescUpdatedAtDesc(UUID projectId, UUID userId);
    Optional<AiConversation> findByIdAndUserId(UUID id, UUID userId);
}
