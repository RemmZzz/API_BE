package com.apibe.API_BE.module.collection.repository;

import com.apibe.API_BE.module.collection.entity.ApiCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiCollectionRepository extends JpaRepository<ApiCollection, UUID> {
    List<ApiCollection> findByProjectId(UUID projectId);
}
