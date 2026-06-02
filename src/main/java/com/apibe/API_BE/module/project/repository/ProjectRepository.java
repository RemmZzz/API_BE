package com.apibe.API_BE.module.project.repository;

import com.apibe.API_BE.module.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
}

