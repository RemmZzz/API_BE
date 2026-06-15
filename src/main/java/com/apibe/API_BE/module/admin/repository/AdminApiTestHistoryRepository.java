package com.apibe.API_BE.module.admin.repository;

import com.apibe.API_BE.module.apitester.entity.ApiTestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminApiTestHistoryRepository extends JpaRepository<ApiTestHistory, UUID> {
}
