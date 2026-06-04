package com.apibe.API_BE.module.admin.repository;

import com.apibe.API_BE.module.admin.repository.projection.CountByStatusProjection;
import com.apibe.API_BE.module.admin.repository.projection.RevenueItemProjection;
import com.apibe.API_BE.module.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
public interface AdminPaymentRepository extends JpaRepository<Payment, UUID> {

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status IN ('PAID', 'SUCCESS')", nativeQuery = true)
    BigDecimal sumSuccessfulRevenue();

    @Query(value = "SELECT status AS status, COUNT(*) AS count FROM payments GROUP BY status", nativeQuery = true)
    List<CountByStatusProjection> countGroupByStatus();

    List<Payment> findTop10ByOrderByCreatedAtDesc();

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM payments
            WHERE created_at BETWEEN :fromDate AND :toDate
              AND status IN ('PAID', 'SUCCESS')
              AND (:status IS NULL OR status = :status)
              AND (:cycle IS NULL OR cycle = :cycle)
            """, nativeQuery = true)
    BigDecimal sumSuccessfulRevenueByFilters(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") String status,
            @Param("cycle") String cycle
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM payments
            WHERE created_at BETWEEN :fromDate AND :toDate
              AND (:status IS NULL OR status = :status)
              AND (:cycle IS NULL OR cycle = :cycle)
            """, nativeQuery = true)
    long countByFilters(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") String status,
            @Param("cycle") String cycle
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM payments
            WHERE created_at BETWEEN :fromDate AND :toDate
              AND status IN ('PAID', 'SUCCESS')
              AND (:status IS NULL OR status = :status)
              AND (:cycle IS NULL OR cycle = :cycle)
            """, nativeQuery = true)
    long countSuccessByFilters(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") String status,
            @Param("cycle") String cycle
    );

    @Query(value = """
            SELECT COUNT(*)
            FROM payments
            WHERE created_at BETWEEN :fromDate AND :toDate
              AND status = :targetStatus
              AND (:status IS NULL OR status = :status)
              AND (:cycle IS NULL OR cycle = :cycle)
            """, nativeQuery = true)
    long countStatusByFilters(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("targetStatus") String targetStatus,
            @Param("status") String status,
            @Param("cycle") String cycle
    );

    @Query(value = """
            SELECT
                CASE
                    WHEN :groupBy = 'month' THEN DATE_FORMAT(created_at, '%Y-%m')
                    ELSE DATE_FORMAT(created_at, '%Y-%m-%d')
                END AS period,
                COALESCE(SUM(CASE WHEN status IN ('PAID', 'SUCCESS') THEN amount ELSE 0 END), 0) AS revenue,
                COUNT(*) AS paymentCount
            FROM payments
            WHERE created_at BETWEEN :fromDate AND :toDate
              AND (:status IS NULL OR status = :status)
              AND (:cycle IS NULL OR cycle = :cycle)
            GROUP BY period
            ORDER BY period ASC
            """, nativeQuery = true)
    List<RevenueItemProjection> findRevenueItems(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") String status,
            @Param("cycle") String cycle,
            @Param("groupBy") String groupBy
    );

    @Query(
            value = """
                    SELECT *
                    FROM payments
                    WHERE created_at BETWEEN :fromDate AND :toDate
                      AND (:status IS NULL OR status = :status)
                      AND (:cycle IS NULL OR cycle = :cycle)
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM payments
                    WHERE created_at BETWEEN :fromDate AND :toDate
                      AND (:status IS NULL OR status = :status)
                      AND (:cycle IS NULL OR cycle = :cycle)
                    """,
            nativeQuery = true
    )
    Page<Payment> findPageByFilters(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") String status,
            @Param("cycle") String cycle,
            Pageable pageable
    );
}
