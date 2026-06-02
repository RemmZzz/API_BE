package com.apibe.API_BE.module.user.repository;

import com.apibe.API_BE.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}

