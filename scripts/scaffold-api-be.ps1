$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Ensure-Dir($path) {
    if (-not (Test-Path -LiteralPath $path)) { New-Item -ItemType Directory -Path $path | Out-Null }
}

function Write-File($path, $content, [switch]$NoOverwrite) {
    $full = Join-Path $root $path
    Ensure-Dir (Split-Path $full -Parent)
    if ($NoOverwrite -and (Test-Path -LiteralPath $full)) { return }
    Set-Content -LiteralPath $full -Value $content -Encoding UTF8
}

function Package-Path($package) { return ($package -replace '\.', '\') }
function Write-Java($package, $className, $content) {
    Write-File "src\main\java\$(Package-Path $package)\$className.java" $content
}
function Write-TestJava($package, $className, $content) {
    Write-File "src\test\java\$(Package-Path $package)\$className.java" $content
}
function Service-Code($package, $className) {
    "package $package;`n`nimport org.springframework.stereotype.Service;`n`n@Service`npublic class $className {`n}`n"
}
function Component-Code($package, $className) {
    "package $package;`n`nimport org.springframework.stereotype.Component;`n`n@Component`npublic class $className {`n}`n"
}
function Plain-Code($package, $className) {
    "package $package;`n`npublic class $className {`n}`n"
}
function Controller-Code($module, $className) {
    "package com.apibe.API_BE.module.$module.controller;`n`nimport lombok.RequiredArgsConstructor;`nimport org.springframework.web.bind.annotation.RequestMapping;`nimport org.springframework.web.bind.annotation.RestController;`n`n@RestController`n@RequestMapping(`"/api/$module`")`n@RequiredArgsConstructor`npublic class $className {`n}`n"
}
function Dto-Code($package, $className) {
    "package $package;`n`nimport lombok.*;`n`n@Getter`n@Setter`n@Builder`n@NoArgsConstructor`n@AllArgsConstructor`npublic class $className {`n`n    private String placeholder;`n}`n"
}
function Entity-Code($module, $className, $tableName) {
    "package com.apibe.API_BE.module.$module.entity;`n`nimport jakarta.persistence.*;`nimport lombok.*;`n`nimport java.time.LocalDateTime;`nimport java.util.UUID;`n`n@Getter`n@Setter`n@Builder`n@NoArgsConstructor`n@AllArgsConstructor`n@Entity`n@Table(name = `"$tableName`")`npublic class $className {`n`n    @Id`n    @GeneratedValue(strategy = GenerationType.UUID)`n    @Column(name = `"id`", columnDefinition = `"CHAR(36)`")`n    private UUID id;`n`n    @Column(name = `"created_at`")`n    private LocalDateTime createdAt;`n`n    @Column(name = `"updated_at`")`n    private LocalDateTime updatedAt;`n`n    @PrePersist`n    public void prePersist() {`n        LocalDateTime now = LocalDateTime.now();`n        this.createdAt = now;`n        this.updatedAt = now;`n    }`n`n    @PreUpdate`n    public void preUpdate() {`n        this.updatedAt = LocalDateTime.now();`n    }`n}`n"
}
function Repo-Code($module, $className, $entityName) {
    "package com.apibe.API_BE.module.$module.repository;`n`nimport com.apibe.API_BE.module.$module.entity.$entityName;`nimport org.springframework.data.jpa.repository.JpaRepository;`n`nimport java.util.UUID;`n`npublic interface $className extends JpaRepository<$entityName, UUID> {`n}`n"
}
function Table-Name($className) {
    ($className -creplace '([a-z0-9])([A-Z])', '$1_$2').ToLowerInvariant()
}

$oldMain = Join-Path $root 'src\main\java\com\apife\backend'
if (Test-Path -LiteralPath $oldMain) { Remove-Item -LiteralPath $oldMain -Recurse -Force }
$oldMainClass = Join-Path $root 'src\main\java\com\apibe\API_BE\ApiBeApplication.java'
if (Test-Path -LiteralPath $oldMainClass) { Remove-Item -LiteralPath $oldMainClass -Force }
$oldTestClass = Join-Path $root 'src\test\java\com\apibe\API_BE\ApiBeApplicationTests.java'
if (Test-Path -LiteralPath $oldTestClass) { Remove-Item -LiteralPath $oldTestClass -Force }
$oldTestPkg = Join-Path $root 'src\test\java\com\apife\backend'
if (Test-Path -LiteralPath $oldTestPkg) { Remove-Item -LiteralPath $oldTestPkg -Recurse -Force }

Write-Java 'com.apibe.API_BE' 'ApiFeBackendApplication' "package com.apibe.API_BE;`n`nimport org.springframework.boot.SpringApplication;`nimport org.springframework.boot.autoconfigure.SpringBootApplication;`n`n@SpringBootApplication`npublic class ApiFeBackendApplication {`n`n    public static void main(String[] args) {`n        SpringApplication.run(ApiFeBackendApplication.class, args);`n    }`n}`n"
Write-TestJava 'com.apibe.API_BE' 'ApiFeBackendApplicationTests' "package com.apibe.API_BE;`n`nimport org.junit.jupiter.api.Test;`nimport org.springframework.boot.test.context.SpringBootTest;`n`n@SpringBootTest(properties = {`n        `"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration`"`n})`nclass ApiFeBackendApplicationTests {`n`n    @Test`n    void contextLoads() {`n    }`n}`n"

Write-Java 'com.apibe.API_BE.global.config' 'SecurityConfig' "package com.apibe.API_BE.global.config;`n`nimport org.springframework.context.annotation.Bean;`nimport org.springframework.context.annotation.Configuration;`nimport org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;`nimport org.springframework.security.crypto.password.PasswordEncoder;`n`n@Configuration`npublic class SecurityConfig {`n`n    @Bean`n    public PasswordEncoder passwordEncoder() {`n        return new BCryptPasswordEncoder();`n    }`n}`n"
foreach ($c in @('CorsConfig','OpenApiConfig','WebClientConfig')) { Write-Java 'com.apibe.API_BE.global.config' $c "package com.apibe.API_BE.global.config;`n`nimport org.springframework.context.annotation.Configuration;`n`n@Configuration`npublic class $c {`n}`n" }
foreach ($c in @('JwtAuthenticationFilter','JwtTokenProvider','CustomUserDetails','CustomUserDetailsService','SecurityUtils')) { Write-Java 'com.apibe.API_BE.global.security' $c (Plain-Code 'com.apibe.API_BE.global.security' $c) }

Write-Java 'com.apibe.API_BE.global.exception' 'AppException' "package com.apibe.API_BE.global.exception;`n`nimport lombok.Getter;`n`n@Getter`npublic class AppException extends RuntimeException {`n`n    private final ErrorCode errorCode;`n`n    public AppException(ErrorCode errorCode) {`n        super(errorCode.getMessage());`n        this.errorCode = errorCode;`n    }`n`n    public AppException(ErrorCode errorCode, String message) {`n        super(message);`n        this.errorCode = errorCode;`n    }`n}`n"
Write-Java 'com.apibe.API_BE.global.exception' 'ErrorCode' "package com.apibe.API_BE.global.exception;`n`nimport lombok.Getter;`nimport org.springframework.http.HttpStatus;`n`n@Getter`npublic enum ErrorCode {`n`n    BAD_REQUEST(`"BAD_REQUEST`", `"Bad request`", HttpStatus.BAD_REQUEST),`n    UNAUTHORIZED(`"UNAUTHORIZED`", `"Unauthorized`", HttpStatus.UNAUTHORIZED),`n    FORBIDDEN(`"FORBIDDEN`", `"Forbidden`", HttpStatus.FORBIDDEN),`n    NOT_FOUND(`"NOT_FOUND`", `"Resource not found`", HttpStatus.NOT_FOUND),`n    INTERNAL_SERVER_ERROR(`"INTERNAL_SERVER_ERROR`", `"Internal server error`", HttpStatus.INTERNAL_SERVER_ERROR);`n`n    private final String code;`n    private final String message;`n    private final HttpStatus status;`n`n    ErrorCode(String code, String message, HttpStatus status) {`n        this.code = code;`n        this.message = message;`n        this.status = status;`n    }`n}`n"
Write-Java 'com.apibe.API_BE.global.exception' 'GlobalExceptionHandler' "package com.apibe.API_BE.global.exception;`n`nimport com.apibe.API_BE.global.response.ErrorResponse;`nimport org.springframework.http.ResponseEntity;`nimport org.springframework.web.bind.annotation.ExceptionHandler;`nimport org.springframework.web.bind.annotation.RestControllerAdvice;`n`nimport java.time.LocalDateTime;`nimport java.util.List;`n`n@RestControllerAdvice`npublic class GlobalExceptionHandler {`n`n    @ExceptionHandler(AppException.class)`n    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {`n        ErrorCode errorCode = ex.getErrorCode();`n`n        ErrorResponse response = ErrorResponse.builder()`n                .success(false)`n                .message(ex.getMessage())`n                .errorCode(errorCode.getCode())`n                .errors(List.of(ex.getMessage()))`n                .timestamp(LocalDateTime.now())`n                .build();`n`n        return ResponseEntity.status(errorCode.getStatus()).body(response);`n    }`n`n    @ExceptionHandler(Exception.class)`n    public ResponseEntity<ErrorResponse> handleException(Exception ex) {`n        ErrorResponse response = ErrorResponse.builder()`n                .success(false)`n                .message(`"Internal server error`")`n                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())`n                .errors(List.of(ex.getMessage()))`n                .timestamp(LocalDateTime.now())`n                .build();`n`n        return ResponseEntity.status(500).body(response);`n    }`n}`n"

Write-Java 'com.apibe.API_BE.global.response' 'ApiResponse' "package com.apibe.API_BE.global.response;`n`nimport lombok.*;`n`nimport java.time.LocalDateTime;`n`n@Getter`n@Setter`n@Builder`n@NoArgsConstructor`n@AllArgsConstructor`npublic class ApiResponse<T> {`n`n    private boolean success;`n    private String message;`n    private T data;`n    private LocalDateTime timestamp;`n`n    public static <T> ApiResponse<T> success(T data) {`n        return ApiResponse.<T>builder()`n                .success(true)`n                .message(`"Success`")`n                .data(data)`n                .timestamp(LocalDateTime.now())`n                .build();`n    }`n`n    public static <T> ApiResponse<T> success(String message, T data) {`n        return ApiResponse.<T>builder()`n                .success(true)`n                .message(message)`n                .data(data)`n                .timestamp(LocalDateTime.now())`n                .build();`n    }`n`n    public static <T> ApiResponse<T> error(String message) {`n        return ApiResponse.<T>builder()`n                .success(false)`n                .message(message)`n                .data(null)`n                .timestamp(LocalDateTime.now())`n                .build();`n    }`n}`n"
Write-Java 'com.apibe.API_BE.global.response' 'PageResponse' "package com.apibe.API_BE.global.response;`n`nimport lombok.*;`n`nimport java.util.List;`n`n@Getter`n@Setter`n@Builder`n@NoArgsConstructor`n@AllArgsConstructor`npublic class PageResponse<T> {`n`n    private List<T> items;`n    private int page;`n    private int size;`n    private long totalItems;`n    private int totalPages;`n    private boolean hasNext;`n    private boolean hasPrevious;`n}`n"
Write-Java 'com.apibe.API_BE.global.response' 'ErrorResponse' "package com.apibe.API_BE.global.response;`n`nimport lombok.*;`n`nimport java.time.LocalDateTime;`nimport java.util.List;`n`n@Getter`n@Setter`n@Builder`n@NoArgsConstructor`n@AllArgsConstructor`npublic class ErrorResponse {`n`n    private boolean success;`n    private String message;`n    private String errorCode;`n    private List<String> errors;`n    private LocalDateTime timestamp;`n}`n"

$enums = @{ UserRole='USER,ADMIN'; UserStatus='ACTIVE,INACTIVE,BANNED,PENDING'; ProjectStatus='ACTIVE,ARCHIVED,DELETED'; MemberRole='OWNER,ADMIN,MEMBER,VIEWER'; HttpMethodType='GET,POST,PUT,PATCH,DELETE'; PaymentStatus='PENDING,PAID,SUCCESS,FAILED,CANCELLED,EXPIRED'; SubscriptionStatus='ACTIVE,INACTIVE,CANCELLED,EXPIRED' }
foreach ($kv in $enums.GetEnumerator()) {
    $body = ($kv.Value -split ',') -join ",`n    "
    Write-Java 'com.apibe.API_BE.global.enums' $kv.Key "package com.apibe.API_BE.global.enums;`n`npublic enum $($kv.Key) {`n    $body`n}`n"
}
foreach ($c in @('DateTimeUtils','JsonUtils','PasswordUtils','ValidationUtils')) { Write-Java 'com.apibe.API_BE.global.util' $c "package com.apibe.API_BE.global.util;`n`npublic final class $c {`n`n    private $c() {`n    }`n}`n" }

Write-Java 'com.apibe.API_BE.module.auth.controller' 'AuthController' (Controller-Code 'auth' 'AuthController')
foreach ($c in @('AuthService','OtpService','PasswordResetService')) { Write-Java 'com.apibe.API_BE.module.auth.service' $c (Service-Code 'com.apibe.API_BE.module.auth.service' $c) }
foreach ($c in @('LoginRequest','RegisterRequest','VerifyOtpRequest','ForgotPasswordRequest','ResetPasswordRequest')) { Write-Java 'com.apibe.API_BE.module.auth.dto.request' $c (Dto-Code 'com.apibe.API_BE.module.auth.dto.request' $c) }
foreach ($c in @('AuthResponse','TokenResponse')) { Write-Java 'com.apibe.API_BE.module.auth.dto.response' $c (Dto-Code 'com.apibe.API_BE.module.auth.dto.response' $c) }
Write-Java 'com.apibe.API_BE.module.auth.mapper' 'AuthMapper' (Component-Code 'com.apibe.API_BE.module.auth.mapper' 'AuthMapper')

foreach ($c in @('UserController','ProfileController')) { Write-Java 'com.apibe.API_BE.module.user.controller' $c (Controller-Code 'user' $c) }
foreach ($c in @('UserService','ProfileService')) { Write-Java 'com.apibe.API_BE.module.user.service' $c (Service-Code 'com.apibe.API_BE.module.user.service' $c) }
foreach ($c in @('User','UserSession','UserSetting','OtpVerification','PasswordResetToken','ApiKey')) { Write-Java 'com.apibe.API_BE.module.user.entity' $c (Entity-Code 'user' $c (Table-Name $c)) }
foreach ($kv in @{ UserRepository='User'; UserSessionRepository='UserSession'; OtpVerificationRepository='OtpVerification'; PasswordResetTokenRepository='PasswordResetToken' }.GetEnumerator()) { Write-Java 'com.apibe.API_BE.module.user.repository' $kv.Key (Repo-Code 'user' $kv.Key $kv.Value) }
foreach ($c in @('UpdateProfileRequest','ChangePasswordRequest','UpdateSettingRequest')) { Write-Java 'com.apibe.API_BE.module.user.dto.request' $c (Dto-Code 'com.apibe.API_BE.module.user.dto.request' $c) }
foreach ($c in @('UserResponse','ProfileResponse','UserSettingResponse')) { Write-Java 'com.apibe.API_BE.module.user.dto.response' $c (Dto-Code 'com.apibe.API_BE.module.user.dto.response' $c) }
Write-Java 'com.apibe.API_BE.module.user.mapper' 'UserMapper' (Component-Code 'com.apibe.API_BE.module.user.mapper' 'UserMapper')

Write-Java 'com.apibe.API_BE.module.project.controller' 'ProjectController' (Controller-Code 'project' 'ProjectController')
foreach ($c in @('ProjectService','ProjectPermissionService')) { Write-Java 'com.apibe.API_BE.module.project.service' $c (Service-Code 'com.apibe.API_BE.module.project.service' $c) }
foreach ($c in @('Project','ProjectMember')) { Write-Java 'com.apibe.API_BE.module.project.entity' $c (Entity-Code 'project' $c (Table-Name $c)) }
foreach ($kv in @{ ProjectRepository='Project'; ProjectMemberRepository='ProjectMember' }.GetEnumerator()) { Write-Java 'com.apibe.API_BE.module.project.repository' $kv.Key (Repo-Code 'project' $kv.Key $kv.Value) }
foreach ($c in @('CreateProjectRequest','UpdateProjectRequest')) { Write-Java 'com.apibe.API_BE.module.project.dto.request' $c (Dto-Code 'com.apibe.API_BE.module.project.dto.request' $c) }
foreach ($c in @('ProjectResponse','ProjectDetailResponse')) { Write-Java 'com.apibe.API_BE.module.project.dto.response' $c (Dto-Code 'com.apibe.API_BE.module.project.dto.response' $c) }
Write-Java 'com.apibe.API_BE.module.project.mapper' 'ProjectMapper' (Component-Code 'com.apibe.API_BE.module.project.mapper' 'ProjectMapper')

foreach ($c in @('DatabaseSchemaController','DatabaseTableController','DatabaseColumnController','DatabaseRelationshipController')) { Write-Java 'com.apibe.API_BE.module.database.controller' $c (Controller-Code 'database' $c) }
foreach ($c in @('DatabaseSchemaService','DatabaseTableService','DatabaseColumnService','DatabaseRelationshipService','SqlGeneratorService')) { Write-Java 'com.apibe.API_BE.module.database.service' $c (Service-Code 'com.apibe.API_BE.module.database.service' $c) }
foreach ($c in @('DatabaseSchema','DatabaseTable','DatabaseColumn','DatabaseRelationship','DatabaseIndex')) { Write-Java 'com.apibe.API_BE.module.database.entity' $c (Entity-Code 'database' $c (Table-Name $c)) }
foreach ($kv in @{ DatabaseSchemaRepository='DatabaseSchema'; DatabaseTableRepository='DatabaseTable'; DatabaseColumnRepository='DatabaseColumn'; DatabaseRelationshipRepository='DatabaseRelationship'; DatabaseIndexRepository='DatabaseIndex' }.GetEnumerator()) { Write-Java 'com.apibe.API_BE.module.database.repository' $kv.Key (Repo-Code 'database' $kv.Key $kv.Value) }
foreach ($c in @('SaveDatabaseSchemaRequest','CreateTableRequest','UpdateTableRequest','CreateColumnRequest','UpdateColumnRequest','CreateRelationshipRequest')) { Write-Java 'com.apibe.API_BE.module.database.dto.request' $c (Dto-Code 'com.apibe.API_BE.module.database.dto.request' $c) }
foreach ($c in @('DatabaseSchemaResponse','DatabaseTableResponse','DatabaseColumnResponse','SqlPreviewResponse')) { Write-Java 'com.apibe.API_BE.module.database.dto.response' $c (Dto-Code 'com.apibe.API_BE.module.database.dto.response' $c) }
Write-Java 'com.apibe.API_BE.module.database.mapper' 'DatabaseMapper' (Component-Code 'com.apibe.API_BE.module.database.mapper' 'DatabaseMapper')

foreach ($m in @('collection','environment','apitester','documentation','mockserver','workspace','subscription','payment','activity','admin')) {
    foreach ($p in @('controller','service','repository','entity','dto\request','dto\response','mapper')) {
        Ensure-Dir (Join-Path $root "src\main\java\com\apibe\API_BE\module\$m\$p")
        Write-File "src\main\java\com\apibe\API_BE\module\$m\$p\.gitkeep" ''
    }
}

foreach ($c in @('EmailService','SmtpEmailService')) { Write-Java 'com.apibe.API_BE.infrastructure.email' $c (Plain-Code 'com.apibe.API_BE.infrastructure.email' $c) }
foreach ($c in @('FileStorageService','LocalFileStorageService')) { Write-Java 'com.apibe.API_BE.infrastructure.storage' $c (Plain-Code 'com.apibe.API_BE.infrastructure.storage' $c) }
foreach ($c in @('AiClient','MockAiClient')) { Write-Java 'com.apibe.API_BE.infrastructure.ai' $c (Plain-Code 'com.apibe.API_BE.infrastructure.ai' $c) }
foreach ($c in @('HttpClientService','WebClientHttpClientService')) { Write-Java 'com.apibe.API_BE.infrastructure.http' $c (Plain-Code 'com.apibe.API_BE.infrastructure.http' $c) }

Write-File 'src\main\resources\application.yml' "server:`n  port: 8080`n`nspring:`n  application:`n    name: api-be`n`n  profiles:`n    active: dev`n"
Write-File 'src\main\resources\application-dev.yml' "spring:`n  datasource:`n    url: jdbc:mysql://localhost:3306/api_fe_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8`n    username: root`n    password: your_mysql_password`n    driver-class-name: com.mysql.cj.jdbc.Driver`n`n  jpa:`n    hibernate:`n      ddl-auto: update`n    show-sql: true`n    properties:`n      hibernate:`n        format_sql: true`n"
Write-File 'src\main\resources\application-prod.yml' "spring:`n  datasource:`n    url: jdbc:mysql://`${DB_HOST}:`${DB_PORT}/`${DB_NAME}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8`n    username: `${DB_USERNAME}`n    password: `${DB_PASSWORD}`n    driver-class-name: com.mysql.cj.jdbc.Driver`n`n  jpa:`n    hibernate:`n      ddl-auto: validate`n    show-sql: false`n"
foreach ($sql in @('V1__init_users.sql','V2__init_projects.sql','V3__init_database_designer.sql','V4__init_api_collections.sql','V5__init_environments.sql','V6__init_api_tester.sql','V7__init_documentation_mock.sql','V8__init_payment_subscription.sql','V9__add_indexes.sql')) { Write-File "src\main\resources\db\migration\$sql" '' }
Write-File 'src\main\resources\templates\email\otp-email.html' ''
Write-File 'src\main\resources\templates\email\reset-password-email.html' ''
Ensure-Dir (Join-Path $root 'src\main\resources\static')

foreach ($d in @('auth','user','project','database','collection','environment','apitester')) {
    Ensure-Dir (Join-Path $root "src\test\java\com\apibe\API_BE\$d")
    Write-File "src\test\java\com\apibe\API_BE\$d\.gitkeep" ''
}

Write-File 'docs\api-design.md' '# API Design'
Write-File 'docs\database-design.md' '# Database Design'
Write-File 'docs\postman-collection.json' '{"info":{"name":"API_BE","schema":"https://schema.getpostman.com/json/collection/v2.1.0/collection.json"},"item":[]}'
Write-File 'README.md' '# API_BE' -NoOverwrite
Write-File '.env.example' "DB_HOST=localhost`nDB_PORT=3306`nDB_NAME=api_fe_db`nDB_USERNAME=root`nDB_PASSWORD=your_mysql_password`n" -NoOverwrite
Write-File 'docker-compose.yml' "services:`n  mysql:`n    image: mysql:8.0`n    container_name: api_be_mysql`n    environment:`n      MYSQL_ROOT_PASSWORD: your_mysql_password`n      MYSQL_DATABASE: api_fe_db`n    ports:`n      - `"3306:3306`"`n" -NoOverwrite

Write-Output 'Generated skeleton.'
