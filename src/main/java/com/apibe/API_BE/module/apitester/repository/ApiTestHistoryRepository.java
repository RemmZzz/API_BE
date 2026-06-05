package com.apibe.API_BE.module.apitester.repository;

import com.apibe.API_BE.global.enums.HttpMethodType;
import com.apibe.API_BE.module.apitester.entity.ApiTestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ApiTestHistoryRepository extends JpaRepository<ApiTestHistory, UUID> {

    /** All history for a project, paginated */
    Page<ApiTestHistory> findByProjectId(UUID projectId, Pageable pageable);

    /** Filter by project + method, paginated */
    Page<ApiTestHistory> findByProjectIdAndMethod(UUID projectId, HttpMethodType method, Pageable pageable);

    /** Keyword search (name or url) within a project, paginated */
    @Query("SELECT h FROM ApiTestHistory h WHERE h.projectId = :projectId " +
           "AND (LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(h.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ApiTestHistory> findByProjectIdAndKeyword(
            @Param("projectId") UUID projectId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /** Keyword + method filter within a project, paginated */
    @Query("SELECT h FROM ApiTestHistory h WHERE h.projectId = :projectId " +
           "AND h.method = :method " +
           "AND (LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(h.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ApiTestHistory> findByProjectIdAndMethodAndKeyword(
            @Param("projectId") UUID projectId,
            @Param("method") HttpMethodType method,
            @Param("keyword") String keyword,
            Pageable pageable);
}
