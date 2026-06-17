package com.apibe.API_BE.module.collection.repository;

import com.apibe.API_BE.module.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    List<Collection> findByProjectIdOrderBySortOrderAscCreatedAtAsc(UUID projectId);
}
