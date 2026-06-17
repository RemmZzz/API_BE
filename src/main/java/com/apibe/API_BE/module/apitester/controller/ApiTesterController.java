package com.apibe.API_BE.module.apitester.controller;

import com.apibe.API_BE.global.response.ApiResponse;
import com.apibe.API_BE.global.response.PageResponse;
import com.apibe.API_BE.module.apitester.dto.request.ApiTestSendRequest;
import com.apibe.API_BE.module.apitester.dto.response.ApiTestHistoryResponse;
import com.apibe.API_BE.module.apitester.dto.response.ApiTestSendResponse;
import com.apibe.API_BE.module.apitester.service.ApiTesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApiTesterController {

    private final ApiTesterService apiTesterService;

    // ── T040 ──────────────────────────────────────────────────────────────────
    // POST /api/api-tester/send
    // Gọi API ngoài bằng server-side RestClient, đo duration và lưu history.
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/api/api-tester/send")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ApiTestSendResponse> send(
            @Valid @RequestBody ApiTestSendRequest request) {
        ApiTestSendResponse result = apiTesterService.send(request);
        return ApiResponse.success("Request sent successfully", result);
    }

    // ── T041 ──────────────────────────────────────────────────────────────────
    // GET /api/projects/{projectId}/api-test-history
    // Lấy lịch sử request theo project, method, keyword (paginated).
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/api/projects/{projectId}/api-test-history")
    public ApiResponse<PageResponse<ApiTestHistoryResponse>> getHistory(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = "createdAt";

        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",", 2);
            sortField = parts[0].trim();
            try {
                direction = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ignored) {
                // keep default DESC
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        PageResponse<ApiTestHistoryResponse> result =
                apiTesterService.getProjectHistory(projectId, method, keyword, pageable);

        return ApiResponse.success("History retrieved successfully", result);
    }

    // ── T042 ──────────────────────────────────────────────────────────────────
    // DELETE /api/test-history/{historyId}
    // Xóa một lịch sử test API.
    // ─────────────────────────────────────────────────────────────────────────

    @DeleteMapping("/api/test-history/{historyId}")
    public ApiResponse<Void> deleteHistory(@PathVariable UUID historyId) {
        apiTesterService.deleteHistory(historyId);
        return ApiResponse.success("History deleted successfully", null);
    }
}
