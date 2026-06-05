package com.apibe.API_BE.module.apitester.service;

import com.apibe.API_BE.global.enums.HttpMethodType;
import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.apibe.API_BE.global.response.PageResponse;
import com.apibe.API_BE.module.apitester.dto.request.ApiTestSendRequest;
import com.apibe.API_BE.module.apitester.dto.response.ApiTestHistoryResponse;
import com.apibe.API_BE.module.apitester.dto.response.ApiTestSendResponse;
import com.apibe.API_BE.module.apitester.entity.ApiTestHistory;
import com.apibe.API_BE.module.apitester.repository.ApiTestHistoryRepository;
import com.apibe.API_BE.module.project.repository.ProjectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiTesterService {

    private static final int         MAX_TIMEOUT_MS          = 60_000;
    private static final int         MAX_RESPONSE_BODY_CHARS = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_SCHEMES         = Set.of("http", "https");
    private static final List<String> BLOCKED_HOSTS          = List.of(
            "localhost", "127.0.0.1", "0.0.0.0", "::1"
    );

    private final ApiTestHistoryRepository historyRepository;
    private final ProjectRepository        projectRepository;
    private final ObjectMapper             objectMapper;
    private final RestClient.Builder       restClientBuilder;

    // ── T040: Send request ────────────────────────────────────────────────────

    public ApiTestSendResponse send(ApiTestSendRequest req) {

        // 1. Validate project exists
        if (!projectRepository.existsById(req.getProjectId())) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found: " + req.getProjectId());
        }

        // 2. SSRF / URL safety validation — throws BAD_REQUEST on violation
        String safeUrl = validateAndBuildUrl(req.getUrl(), req.getParams());

        // 3. Clamp timeout (max 60 s)
        int timeoutMs = Math.min(
                req.getTimeoutMs() > 0 ? req.getTimeoutMs() : 30_000,
                MAX_TIMEOUT_MS);

        // 4. Serialise request fields for storage
        String reqHeadersJson = toJson(req.getHeaders());
        String reqParamsJson  = toJson(req.getParams());
        String reqBodyJson    = toJson(req.getBody());

        // 5. Build a per-request RestClient with the correct timeout
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        RestClient client = restClientBuilder.clone()
                .requestFactory(factory)
                .build();

        // 6. Execute HTTP request
        long    startNano           = System.nanoTime();
        Integer statusCode          = null;
        String  statusText          = null;
        String  responseBodyStr     = null;
        String  responseHeadersJson = null;
        String  errorMessage        = null;
        boolean success             = false;

        try {
            HttpMethod httpMethod = HttpMethod.valueOf(req.getMethod().name());

            // Build the base request spec with URI + custom headers
            RestClient.RequestBodySpec bodySpec = client
                    .method(httpMethod)
                    .uri(URI.create(safeUrl))
                    .headers(h -> {
                        if (req.getHeaders() != null) {
                            req.getHeaders().forEach(h::set);
                        }
                    });

            ResponseEntity<String> response;

            if (req.getBody() != null && supportsBody(req.getMethod())) {
                // Attach body for POST / PUT / PATCH / DELETE
                response = bodySpec
                        .contentType(MediaType.parseMediaType(
                                resolveContentType(req.getHeaders())))
                        .body(reqBodyJson)
                        .retrieve()
                        // Do NOT throw on 4xx/5xx — we record the status instead
                        .onStatus(HttpStatusCode::isError,
                                (httpReq, clientResp) -> { /* no-op */ })
                        .toEntity(String.class);
            } else {
                response = bodySpec
                        .retrieve()
                        .onStatus(HttpStatusCode::isError,
                                (httpReq, clientResp) -> { /* no-op */ })
                        .toEntity(String.class);
            }

            statusCode          = response.getStatusCode().value();
            statusText          = response.getStatusCode().toString();
            responseHeadersJson = toJson(response.getHeaders().toSingleValueMap());
            responseBodyStr     = truncateIfNeeded(response.getBody());
            success             = true;   // got a response — even 4xx/5xx counts

        } catch (RestClientResponseException ex) {
            // Fallback — guard if onStatus no-op still rethrows in some Spring versions
            statusCode          = ex.getStatusCode().value();
            statusText          = ex.getStatusText();
            responseBodyStr     = truncateIfNeeded(ex.getResponseBodyAsString());
            responseHeadersJson = ex.getResponseHeaders() != null
                    ? toJson(ex.getResponseHeaders().toSingleValueMap()) : null;
            success             = true;   // server did respond

        } catch (RestClientException ex) {
            // Network / timeout / DNS failure — no response received
            errorMessage = ex.getMessage();
            log.warn("[ApiTester] Outbound request failed: {}", ex.getMessage());

        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, ex.getMessage());
        }

        long durationMs = (System.nanoTime() - startNano) / 1_000_000L;

        // 7. Persist history record
        ApiTestHistory saved = null;
        if (req.isSaveHistory()) {
            ApiTestHistory record = ApiTestHistory.builder()
                    .projectId(req.getProjectId())
                    .name(req.getName())
                    .method(req.getMethod())
                    .url(req.getUrl())
                    .requestHeaders(reqHeadersJson)
                    .requestParams(reqParamsJson)
                    .requestBody(reqBodyJson)
                    .statusCode(statusCode)
                    .statusText(statusText)
                    .responseHeaders(responseHeadersJson)
                    .responseBody(responseBodyStr)
                    .durationMs(durationMs)
                    .success(success)
                    .errorMessage(errorMessage)
                    .build();
            saved = historyRepository.save(record);
        }

        // 8. Return send response
        return ApiTestSendResponse.builder()
                .historyId(saved != null ? saved.getId() : null)
                .projectId(req.getProjectId())
                .method(req.getMethod().name())
                .url(req.getUrl())
                .requestHeaders(reqHeadersJson)
                .requestParams(reqParamsJson)
                .requestBody(reqBodyJson)
                .statusCode(statusCode)
                .statusText(statusText)
                .responseHeaders(responseHeadersJson)
                .responseBody(responseBodyStr)
                .durationMs(durationMs)
                .success(success)
                .errorMessage(errorMessage)
                .createdAt(saved != null ? saved.getCreatedAt() : null)
                .build();
    }

    // ── T041: Get project history ─────────────────────────────────────────────

    public PageResponse<ApiTestHistoryResponse> getProjectHistory(
            UUID projectId,
            String method,
            String keyword,
            org.springframework.data.domain.Pageable pageable) {

        if (!projectRepository.existsById(projectId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found: " + projectId);
        }

        HttpMethodType methodEnum = null;
        if (method != null && !method.isBlank()) {
            try {
                methodEnum = HttpMethodType.valueOf(method.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Invalid HTTP method: " + method);
            }
        }

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasMethod  = methodEnum != null;

        org.springframework.data.domain.Page<ApiTestHistory> page;
        if (hasMethod && hasKeyword) {
            page = historyRepository.findByProjectIdAndMethodAndKeyword(
                    projectId, methodEnum, keyword, pageable);
        } else if (hasMethod) {
            page = historyRepository.findByProjectIdAndMethod(projectId, methodEnum, pageable);
        } else if (hasKeyword) {
            page = historyRepository.findByProjectIdAndKeyword(projectId, keyword, pageable);
        } else {
            page = historyRepository.findByProjectId(projectId, pageable);
        }

        List<ApiTestHistoryResponse> items = page.getContent()
                .stream()
                .map(this::toHistoryResponse)
                .toList();

        return PageResponse.<ApiTestHistoryResponse>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    // ── T042: Delete history ──────────────────────────────────────────────────

    public void deleteHistory(UUID historyId) {
        ApiTestHistory record = historyRepository.findById(historyId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,
                        "Test history not found: " + historyId));

        // TODO: when auth/ownership is implemented, verify the current user
        //       has access to record.getProjectId() before deleting.

        historyRepository.delete(record);
    }

    // ── SSRF / URL validation ─────────────────────────────────────────────────

    /**
     * Validates the URL for SSRF safety and appends query params.
     * Throws {@code AppException(BAD_REQUEST)} on any violation.
     */
    private String validateAndBuildUrl(String rawUrl, Map<String, String> params) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "URL must not be empty");
        }

        String trimmed = rawUrl.trim();

        URL url;
        try {
            url = new URL(trimmed);
        } catch (MalformedURLException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Malformed URL: " + trimmed);
        }

        // Scheme whitelist
        String scheme = url.getProtocol();
        if (!ALLOWED_SCHEMES.contains(scheme)) {
            throw new AppException(ErrorCode.BAD_REQUEST,
                    "Only http and https schemes are allowed. Got: " + scheme);
        }

        String host = url.getHost().toLowerCase();
        if (host.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "URL host must not be empty");
        }

        // Block known loopback / ANY hostnames
        for (String blocked : BLOCKED_HOSTS) {
            if (host.equals(blocked)) {
                throw new AppException(ErrorCode.BAD_REQUEST,
                        "Requests to loopback/internal addresses are not allowed");
            }
        }

        // Block private IP ranges via InetAddress
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress()
                    || addr.isSiteLocalAddress()
                    || addr.isLinkLocalAddress()
                    || addr.isAnyLocalAddress()) {
                throw new AppException(ErrorCode.BAD_REQUEST,
                        "Requests to private/internal network addresses are not allowed");
            }
        } catch (AppException ex) {
            throw ex;   // re-throw our own exception
        } catch (Exception ex) {
            // DNS resolution failure at validation time — allow through;
            // the actual HTTP call will fail at the network level.
            log.debug("[ApiTester] Could not pre-resolve host {} for SSRF check: {}", host, ex.getMessage());
        }

        // Append query params if provided
        if (params != null && !params.isEmpty()) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(trimmed);
            params.forEach(builder::queryParam);
            return builder.build().toUriString();
        }

        return trimmed;
    }

    // ── Misc helpers ──────────────────────────────────────────────────────────

    private boolean supportsBody(HttpMethodType method) {
        return switch (method) {
            case POST, PUT, PATCH, DELETE -> true;
            default -> false;
        };
    }

    private String resolveContentType(Map<String, String> headers) {
        if (headers == null) return "application/json";
        return headers.entrySet().stream()
                .filter(e -> "content-type".equalsIgnoreCase(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("application/json");
    }

    private String truncateIfNeeded(String body) {
        if (body == null) return null;
        if (body.length() > MAX_RESPONSE_BODY_CHARS) {
            return body.substring(0, MAX_RESPONSE_BODY_CHARS) + "\n[TRUNCATED]";
        }
        return body;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String str) return str;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

    private ApiTestHistoryResponse toHistoryResponse(ApiTestHistory h) {
        return ApiTestHistoryResponse.builder()
                .id(h.getId())
                .projectId(h.getProjectId())
                .name(h.getName())
                .method(h.getMethod() != null ? h.getMethod().name() : null)
                .url(h.getUrl())
                .requestHeaders(h.getRequestHeaders())
                .requestParams(h.getRequestParams())
                .requestBody(h.getRequestBody())
                .statusCode(h.getStatusCode())
                .statusText(h.getStatusText())
                .responseHeaders(h.getResponseHeaders())
                .responseBody(h.getResponseBody())
                .durationMs(h.getDurationMs())
                .success(h.isSuccess())
                .errorMessage(h.getErrorMessage())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .build();
    }
}
