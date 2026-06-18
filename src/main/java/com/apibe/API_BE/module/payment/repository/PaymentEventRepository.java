package com.apibe.API_BE.module.payment.repository;

import com.apibe.API_BE.module.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {
}
