package com.apibe.API_BE.module.project.repository;

import com.apibe.API_BE.global.enums.ProjectStatus;
import com.apibe.API_BE.module.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("SELECT p FROM Project p WHERE (p.ownerId = :userId OR p.id IN (SELECT pm.projectId FROM ProjectMember pm WHERE pm.userId = :userId)) AND p.status != com.apibe.API_BE.global.enums.ProjectStatus.DELETED")
    List<Project> findByUserAccess(@Param("userId") UUID userId);

    @Query("""
            SELECT p FROM Project p
            WHERE (p.ownerId = :userId OR p.id IN (SELECT pm.projectId FROM ProjectMember pm WHERE pm.userId = :userId))
              AND (:status IS NOT NULL OR p.status != com.apibe.API_BE.global.enums.ProjectStatus.DELETED)
              AND (:status IS NULL OR p.status = :status)
              AND (:type IS NULL OR LOWER(p.type) = LOWER(:type))
              AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Project> findByUserAccess(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("status") ProjectStatus status,
            @Param("type") String type,
            Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.id = :projectId AND (p.ownerId = :userId OR p.id IN (SELECT pm.projectId FROM ProjectMember pm WHERE pm.userId = :userId)) AND p.status != com.apibe.API_BE.global.enums.ProjectStatus.DELETED")
    Optional<Project> findByIdAndUserAccess(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
