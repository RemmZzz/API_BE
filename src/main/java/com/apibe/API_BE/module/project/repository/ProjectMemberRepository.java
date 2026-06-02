package com.apibe.API_BE.module.project.repository;

import com.apibe.API_BE.module.project.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
}

