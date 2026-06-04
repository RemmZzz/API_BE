package com.apibe.API_BE.module.collection.repository;

import com.apibe.API_BE.module.collection.entity.ApiRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiRequestRepository extends JpaRepository<ApiRequest, UUID> {
    List<ApiRequest> findByCollectionId(UUID collectionId);
    List<ApiRequest> findByFolderId(UUID folderId);
}
