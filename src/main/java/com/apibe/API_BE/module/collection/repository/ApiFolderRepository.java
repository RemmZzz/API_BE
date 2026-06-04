package com.apibe.API_BE.module.collection.repository;

import com.apibe.API_BE.module.collection.entity.ApiFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiFolderRepository extends JpaRepository<ApiFolder, UUID> {
    List<ApiFolder> findByCollectionId(UUID collectionId);
}
