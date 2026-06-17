package com.apibe.API_BE.module.admin.repository;

import com.apibe.API_BE.module.admin.repository.projection.CountByStatusProjection;
import com.apibe.API_BE.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Query(value = "SELECT status AS status, COUNT(*) AS count FROM users GROUP BY status", nativeQuery = true)
    List<CountByStatusProjection> countGroupByStatus();
}
