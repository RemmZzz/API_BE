package com.apibe.API_BE.module.admin.repository;

import com.apibe.API_BE.module.activity.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();
}
