package com.apibe.API_BE.module.admin.service;

import com.apibe.API_BE.global.response.PageResponse;
import com.apibe.API_BE.module.admin.dto.request.UpdateUserRoleRequest;
import com.apibe.API_BE.module.admin.dto.request.UpdateUserStatusRequest;
import com.apibe.API_BE.module.admin.dto.response.AdminOverviewResponse;
import com.apibe.API_BE.module.admin.dto.response.AdminUserResponse;
import com.apibe.API_BE.module.admin.dto.response.RevenueReportResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface AdminService {

    AdminOverviewResponse getOverview();

    PageResponse<AdminUserResponse> getUsers(
            int page,
            int size,
            String keyword,
            String role,
            String status,
            String sortBy,
            String sortDir
    );

    AdminUserResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request);

    AdminUserResponse updateUserRole(UUID userId, UpdateUserRoleRequest request);

    RevenueReportResponse getRevenueReport(
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String cycle,
            String groupBy,
            int page,
            int size
    );
}
