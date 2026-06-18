package com.apibe.API_BE.module.payment.repository;

import com.apibe.API_BE.global.enums.PaymentStatus;
import com.apibe.API_BE.module.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderCode(String orderCode);
    List<Payment> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Payment> findAllByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, PaymentStatus status);
    List<Payment> findAllByStatus(PaymentStatus status);
}
