package com.apibe.API_BE.module.subscription.repository;

import com.apibe.API_BE.global.enums.SubscriptionStatus;
import com.apibe.API_BE.module.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findFirstByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, SubscriptionStatus status);
    List<Subscription> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
