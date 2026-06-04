package com.apibe.API_BE.module.admin.service;

import com.apibe.API_BE.global.enums.PaymentStatus;
import com.apibe.API_BE.global.enums.UserRole;
import com.apibe.API_BE.global.enums.UserStatus;
import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.response.PageResponse;
import com.apibe.API_BE.global.security.SecurityUtils;
import com.apibe.API_BE.global.util.JsonUtils;
import com.apibe.API_BE.module.admin.dto.request.UpdateUserRoleRequest;
import com.apibe.API_BE.module.admin.dto.request.UpdateUserStatusRequest;
import com.apibe.API_BE.module.admin.dto.response.*;
import com.apibe.API_BE.module.admin.entity.AdminAuditLog;
import com.apibe.API_BE.module.admin.mapper.AdminMapper;
import com.apibe.API_BE.module.admin.repository.*;
import com.apibe.API_BE.module.payment.entity.Payment;
import com.apibe.API_BE.module.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Set<String> USER_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "username", "email", "role", "status", "lastLoginAt"
    );

    private final AdminUserRepository adminUserRepository;
    private final AdminProjectRepository adminProjectRepository;
    private final AdminPaymentRepository adminPaymentRepository;
    private final AdminApiTestHistoryRepository adminApiTestHistoryRepository;
    private final AdminAiConversationRepository adminAiConversationRepository;
    private final AdminActivityLogRepository adminActivityLogRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AdminMapper adminMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        return AdminOverviewResponse.builder()
                .totalUsers(adminUserRepository.count())
                .totalProjects(adminProjectRepository.count())
                .totalRevenue(defaultZero(adminPaymentRepository.sumSuccessfulRevenue()))
                .totalApiCalls(adminApiTestHistoryRepository.count())
                .totalAiConversations(adminAiConversationRepository.count())
                .usersByStatus(adminUserRepository.countGroupByStatus().stream()
                        .map(adminMapper::toCountByStatusResponse)
                        .toList())
                .projectsByStatus(adminProjectRepository.countGroupByStatus().stream()
                        .map(adminMapper::toCountByStatusResponse)
                        .toList())
                .paymentsByStatus(adminPaymentRepository.countGroupByStatus().stream()
                        .map(adminMapper::toCountByStatusResponse)
                        .toList())
                .recentActivities(adminActivityLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                        .map(adminMapper::toRecentActivityResponse)
                        .toList())
                .recentPayments(adminPaymentRepository.findTop10ByOrderByCreatedAtDesc().stream()
                        .map(adminMapper::toRecentPaymentResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(
            int page,
            int size,
            String keyword,
            String role,
            String status,
            String sortBy,
            String sortDir
    ) {
        UserRole userRole = parseEnum(role, UserRole.class, "role");
        UserStatus userStatus = parseEnum(status, UserStatus.class, "status");
        String safeSortBy = USER_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, safeSortBy));

        Page<User> users = adminUserRepository.findAll(userSpecification(keyword, userRole, userStatus), pageable);
        return toPageResponse(users.map(adminMapper::toAdminUserResponse));
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        validateNotSelf(adminId, userId, "Admin cannot update own status");

        User user = findUser(userId);
        UserStatus oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        User savedUser = adminUserRepository.save(user);

        saveAuditLog(
                adminId,
                userId,
                "UPDATE_USER_STATUS",
                "Admin updated user status from " + oldStatus + " to " + request.getStatus(),
                Map.of(
                        "oldStatus", String.valueOf(oldStatus),
                        "newStatus", String.valueOf(request.getStatus()),
                        "reason", request.getReason() == null ? "" : request.getReason()
                )
        );
        return adminMapper.toAdminUserResponse(savedUser);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(UUID userId, UpdateUserRoleRequest request) {
        UUID adminId = SecurityUtils.getCurrentUserId();
        validateNotSelf(adminId, userId, "Admin cannot update own role");

        User user = findUser(userId);
        UserRole oldRole = user.getRole();
        user.setRole(request.getRole());
        User savedUser = adminUserRepository.save(user);

        saveAuditLog(
                adminId,
                userId,
                "UPDATE_USER_ROLE",
                "Admin updated user role from " + oldRole + " to " + request.getRole(),
                Map.of(
                        "oldRole", String.valueOf(oldRole),
                        "newRole", String.valueOf(request.getRole()),
                        "reason", request.getReason() == null ? "" : request.getReason()
                )
        );
        return adminMapper.toAdminUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String cycle,
            String groupBy,
            int page,
            int size
    ) {
        LocalDate safeFromDate = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate safeToDate = toDate == null ? LocalDate.now() : toDate;
        if (safeFromDate.isAfter(safeToDate)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "fromDate must be before or equal to toDate");
        }

        String safeGroupBy = StringUtils.hasText(groupBy) ? groupBy.toLowerCase(Locale.ROOT) : "day";
        if (!Set.of("day", "month").contains(safeGroupBy)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "groupBy must be day or month");
        }

        String safeCycle = normalizeCycle(cycle);
        PaymentStatus paymentStatus = parseEnum(status, PaymentStatus.class, "status");
        String statusName = paymentStatus == null ? null : paymentStatus.name();
        LocalDateTime from = LocalDateTime.of(safeFromDate, LocalTime.MIN);
        LocalDateTime to = LocalDateTime.of(safeToDate, LocalTime.MAX);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        Page<Payment> paymentPage = adminPaymentRepository.findPageByFilters(from, to, statusName, safeCycle, pageable);
        PageResponse<RecentPaymentResponse> payments = toPageResponse(paymentPage.map(adminMapper::toRecentPaymentResponse));

        return RevenueReportResponse.builder()
                .totalRevenue(defaultZero(adminPaymentRepository.sumSuccessfulRevenueByFilters(from, to, statusName, safeCycle)))
                .totalPayments(adminPaymentRepository.countByFilters(from, to, statusName, safeCycle))
                .successPayments(adminPaymentRepository.countSuccessByFilters(from, to, statusName, safeCycle))
                .failedPayments(adminPaymentRepository.countStatusByFilters(from, to, PaymentStatus.FAILED.name(), statusName, safeCycle))
                .pendingPayments(adminPaymentRepository.countStatusByFilters(from, to, PaymentStatus.PENDING.name(), statusName, safeCycle))
                .revenueItems(adminPaymentRepository.findRevenueItems(from, to, statusName, safeCycle, safeGroupBy).stream()
                        .map(adminMapper::toRevenueItemResponse)
                        .toList())
                .payments(payments)
                .build();
    }

    private User findUser(UUID userId) {
        return adminUserRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateNotSelf(UUID adminId, UUID userId, String message) {
        if (adminId.equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION, message);
        }
    }

    private void saveAuditLog(UUID adminId, UUID targetUserId, String action, String description, Map<String, String> metadata) {
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminId(adminId)
                .targetUserId(targetUserId)
                .action(action)
                .description(description)
                .metadataJson(JsonUtils.toJson(metadata))
                .build());
    }

    private Specification<User> userSpecification(String keyword, UserRole role, UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likeKeyword)
                ));
            }
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizeCycle(String cycle) {
        if (!StringUtils.hasText(cycle)) {
            return null;
        }
        String normalized = cycle.toLowerCase(Locale.ROOT);
        if (!Set.of("monthly", "yearly").contains(normalized)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "cycle must be monthly or yearly");
        }
        return normalized;
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid " + fieldName);
        }
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
