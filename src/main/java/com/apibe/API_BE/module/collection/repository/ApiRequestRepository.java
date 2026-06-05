package com.apibe.API_BE.module.collection.repository;

import com.apibe.API_BE.module.collection.entity.ApiRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiRequestRepository extends JpaRepository<ApiRequest, UUID> {

    /** All requests in a collection */
    List<ApiRequest> findByCollectionIdOrderBySortOrderAscCreatedAtAsc(UUID collectionId);

    /** Requests at collection root level (no folder) */
    List<ApiRequest> findByCollectionIdAndFolderIdIsNullOrderBySortOrderAscCreatedAtAsc(UUID collectionId);

    /** Requests inside a specific folder */
    List<ApiRequest> findByFolderIdOrderBySortOrderAscCreatedAtAsc(UUID folderId);

    /** Validate a request belongs to a specific collection */
    Optional<ApiRequest> findByIdAndCollectionId(UUID id, UUID collectionId);
}
