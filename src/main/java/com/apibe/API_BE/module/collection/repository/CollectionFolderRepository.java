package com.apibe.API_BE.module.collection.repository;

import com.apibe.API_BE.module.collection.entity.CollectionFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionFolderRepository extends JpaRepository<CollectionFolder, UUID> {

    /** All folders belonging to a collection, ordered for display */
    List<CollectionFolder> findByCollectionIdOrderBySortOrderAscCreatedAtAsc(UUID collectionId);

    /** Root folders (no parent) in a collection */
    List<CollectionFolder> findByCollectionIdAndParentFolderIdIsNullOrderBySortOrderAscCreatedAtAsc(UUID collectionId);

    /** Child folders of a given parent */
    List<CollectionFolder> findByParentFolderIdOrderBySortOrderAscCreatedAtAsc(UUID parentFolderId);

    /** Validate a folder belongs to a specific collection */
    Optional<CollectionFolder> findByIdAndCollectionId(UUID id, UUID collectionId);
}
