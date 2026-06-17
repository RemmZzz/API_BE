package com.apibe.API_BE.module.admin.controller;

import com.apibe.API_BE.global.response.ApiResponse;
import com.apibe.API_BE.global.response.PageResponse;
import com.apibe.API_BE.module.admin.dto.request.UpdateUserRoleRequest;
import com.apibe.API_BE.module.admin.dto.request.UpdateUserStatusRequest;
import com.apibe.API_BE.module.admin.dto.response.AdminOverviewResponse;
import com.apibe.API_BE.module.admin.dto.response.AdminUserResponse;
import com.apibe.API_BE.module.admin.dto.response.RevenueReportResponse;
import com.apibe.API_BE.module.admin.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    public ApiResponse<AdminOverviewResponse> getOverview() {
        return ApiResponse.success(adminService.getOverview());
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.success(adminService.getUsers(page, size, keyword, role, status, sortBy, sortDir));
    }

    @PatchMapping("/users/{userId}/status")
    public ApiResponse<AdminUserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success(adminService.updateUserStatus(userId, request));
    }

    @PatchMapping("/users/{userId}/role")
    public ApiResponse<AdminUserResponse> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ApiResponse.success(adminService.updateUserRole(userId, request));
    }

    @GetMapping("/revenue")
    public ApiResponse<RevenueReportResponse> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cycle,
            @RequestParam(defaultValue = "day") String groupBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminService.getRevenueReport(fromDate, toDate, status, cycle, groupBy, page, size));
    }
}
