package com.apibe.API_BE.module.user.repository;

import com.apibe.API_BE.module.user.entity.Oauth2ExchangeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface Oauth2ExchangeCodeRepository extends JpaRepository<Oauth2ExchangeCode, UUID> {

    Optional<Oauth2ExchangeCode> findByCode(String code);
}
