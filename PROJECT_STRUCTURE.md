# Cau truc du an API_BE

File nay mo ta cau truc thu muc chinh cua du an backend Spring Boot/Maven.

> Ghi chu: `.git/` la metadata cua Git va `target/` la thu muc build output cua Maven, nen khong liet ke chi tiet de tranh nhieu. Hai thu muc nay van ton tai trong workspace.

```text
API_BE/
|-- .env.example
|-- .gitattributes
|-- .gitignore
|-- compose.yaml
|-- docker-compose.backup.yaml
|-- HELP.md
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
|-- README.md
|-- PROJECT_STRUCTURE.md
|-- .mvn/
|   `-- wrapper/
|       `-- maven-wrapper.properties
|-- docs/
|   |-- api-design.md
|   |-- database-design.md
|   `-- postman-collection.json
|-- scripts/
|   `-- scaffold-api-be.ps1
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   `-- com/
|   |   |       `-- apibe/
|   |   |           `-- API_BE/
|   |   |               |-- ApiFeBackendApplication.java
|   |   |               |-- global/
|   |   |               |   |-- config/
|   |   |               |   |   |-- CorsConfig.java
|   |   |               |   |   |-- OpenApiConfig.java
|   |   |               |   |   |-- SecurityConfig.java
|   |   |               |   |   `-- WebClientConfig.java
|   |   |               |   |-- enums/
|   |   |               |   |   |-- HttpMethodType.java
|   |   |               |   |   |-- MemberRole.java
|   |   |               |   |   |-- PaymentStatus.java
|   |   |               |   |   |-- ProjectStatus.java
|   |   |               |   |   |-- SubscriptionStatus.java
|   |   |               |   |   |-- UserRole.java
|   |   |               |   |   `-- UserStatus.java
|   |   |               |   |-- exception/
|   |   |               |   |   |-- AppException.java
|   |   |               |   |   |-- ErrorCode.java
|   |   |               |   |   `-- GlobalExceptionHandler.java
|   |   |               |   |-- response/
|   |   |               |   |   |-- ApiResponse.java
|   |   |               |   |   |-- ErrorResponse.java
|   |   |               |   |   `-- PageResponse.java
|   |   |               |   |-- security/
|   |   |               |   |   |-- CustomUserDetails.java
|   |   |               |   |   |-- CustomUserDetailsService.java
|   |   |               |   |   |-- JwtAuthenticationFilter.java
|   |   |               |   |   |-- JwtTokenProvider.java
|   |   |               |   |   `-- SecurityUtils.java
|   |   |               |   `-- util/
|   |   |               |       |-- DateTimeUtils.java
|   |   |               |       |-- JsonUtils.java
|   |   |               |       |-- PasswordUtils.java
|   |   |               |       `-- ValidationUtils.java
|   |   |               |-- infrastructure/
|   |   |               |   |-- ai/
|   |   |               |   |   |-- AiClient.java
|   |   |               |   |   `-- MockAiClient.java
|   |   |               |   |-- email/
|   |   |               |   |   |-- EmailService.java
|   |   |               |   |   `-- SmtpEmailService.java
|   |   |               |   |-- http/
|   |   |               |   |   |-- HttpClientService.java
|   |   |               |   |   `-- WebClientHttpClientService.java
|   |   |               |   `-- storage/
|   |   |               |       |-- FileStorageService.java
|   |   |               |       `-- LocalFileStorageService.java
|   |   |               `-- module/
|   |   |                   |-- activity/
|   |   |                   |-- admin/
|   |   |                   |-- apitester/
|   |   |                   |-- auth/
|   |   |                   |-- collection/
|   |   |                   |-- database/
|   |   |                   |-- documentation/
|   |   |                   |-- environment/
|   |   |                   |-- mockserver/
|   |   |                   |-- payment/
|   |   |                   |-- project/
|   |   |                   |-- subscription/
|   |   |                   |-- user/
|   |   |                   `-- workspace/
|   |   `-- resources/
|   |       |-- application-dev.yml
|   |       |-- application-prod.yml
|   |       |-- application.properties
|   |       |-- application.yml
|   |       |-- db/
|   |       |   `-- migration/
|   |       |       |-- V1__init_users.sql
|   |       |       |-- V2__init_projects.sql
|   |       |       |-- V3__init_database_designer.sql
|   |       |       |-- V4__init_api_collections.sql
|   |       |       |-- V5__init_environments.sql
|   |       |       |-- V6__init_api_tester.sql
|   |       |       |-- V7__init_documentation_mock.sql
|   |       |       |-- V8__init_payment_subscription.sql
|   |       |       `-- V9__add_indexes.sql
|   |       |-- static/
|   |       |   `-- .gitkeep
|   |       `-- templates/
|   |           `-- email/
|   |               |-- otp-email.html
|   |               `-- reset-password-email.html
|   `-- test/
|       `-- java/
|           `-- com/
|               `-- apibe/
|                   `-- API_BE/
|                       |-- ApiFeBackendApplicationTests.java
|                       |-- apitester/
|                       |-- auth/
|                       |-- collection/
|                       |-- database/
|                       |-- environment/
|                       |-- project/
|                       `-- user/
|-- target/
`-- .git/
```

## Cau truc module

Phan lon cac module trong `src/main/java/com/apibe/API_BE/module/` duoc chia theo cac layer quen thuoc:

```text
<module>/
|-- controller/
|-- dto/
|   |-- request/
|   `-- response/
|-- entity/
|-- mapper/
|-- repository/
`-- service/
```

## Module co source code hien tai

### `auth/`

```text
auth/
|-- controller/
|   `-- AuthController.java
|-- dto/
|   |-- request/
|   |   |-- ForgotPasswordRequest.java
|   |   |-- LoginRequest.java
|   |   |-- RegisterRequest.java
|   |   |-- ResetPasswordRequest.java
|   |   `-- VerifyOtpRequest.java
|   `-- response/
|       |-- AuthResponse.java
|       `-- TokenResponse.java
|-- mapper/
|   `-- AuthMapper.java
`-- service/
    |-- AuthService.java
    |-- OtpService.java
    `-- PasswordResetService.java
```

### `database/`

```text
database/
|-- controller/
|   |-- DatabaseColumnController.java
|   |-- DatabaseRelationshipController.java
|   |-- DatabaseSchemaController.java
|   `-- DatabaseTableController.java
|-- dto/
|   |-- request/
|   |   |-- CreateColumnRequest.java
|   |   |-- CreateRelationshipRequest.java
|   |   |-- CreateTableRequest.java
|   |   |-- SaveDatabaseSchemaRequest.java
|   |   |-- UpdateColumnRequest.java
|   |   `-- UpdateTableRequest.java
|   `-- response/
|       |-- DatabaseColumnResponse.java
|       |-- DatabaseSchemaResponse.java
|       |-- DatabaseTableResponse.java
|       `-- SqlPreviewResponse.java
|-- entity/
|   |-- DatabaseColumn.java
|   |-- DatabaseIndex.java
|   |-- DatabaseRelationship.java
|   |-- DatabaseSchema.java
|   `-- DatabaseTable.java
|-- mapper/
|   `-- DatabaseMapper.java
|-- repository/
|   |-- DatabaseColumnRepository.java
|   |-- DatabaseIndexRepository.java
|   |-- DatabaseRelationshipRepository.java
|   |-- DatabaseSchemaRepository.java
|   `-- DatabaseTableRepository.java
`-- service/
    |-- DatabaseColumnService.java
    |-- DatabaseRelationshipService.java
    |-- DatabaseSchemaService.java
    |-- DatabaseTableService.java
    `-- SqlGeneratorService.java
```

### `project/`

```text
project/
|-- controller/
|   `-- ProjectController.java
|-- dto/
|   |-- request/
|   |   |-- CreateProjectRequest.java
|   |   `-- UpdateProjectRequest.java
|   `-- response/
|       |-- ProjectDetailResponse.java
|       `-- ProjectResponse.java
|-- entity/
|   |-- Project.java
|   `-- ProjectMember.java
|-- mapper/
|   `-- ProjectMapper.java
|-- repository/
|   |-- ProjectMemberRepository.java
|   `-- ProjectRepository.java
`-- service/
    |-- ProjectPermissionService.java
    `-- ProjectService.java
```

### `user/`

```text
user/
|-- controller/
|   |-- ProfileController.java
|   `-- UserController.java
|-- dto/
|   |-- request/
|   |   |-- ChangePasswordRequest.java
|   |   |-- UpdateProfileRequest.java
|   |   `-- UpdateSettingRequest.java
|   `-- response/
|       |-- ProfileResponse.java
|       |-- UserResponse.java
|       `-- UserSettingResponse.java
|-- entity/
|   |-- ApiKey.java
|   |-- OtpVerification.java
|   |-- PasswordResetToken.java
|   |-- User.java
|   |-- UserSession.java
|   `-- UserSetting.java
|-- mapper/
|   `-- UserMapper.java
|-- repository/
|   |-- OtpVerificationRepository.java
|   |-- PasswordResetTokenRepository.java
|   |-- UserRepository.java
|   `-- UserSessionRepository.java
`-- service/
    |-- ProfileService.java
    `-- UserService.java
```

## Module dang scaffold

Nhung module sau hien chu yeu co cau truc thu muc va file `.gitkeep`:

- `activity`
- `admin`
- `apitester`
- `collection`
- `documentation`
- `environment`
- `mockserver`
- `payment`
- `subscription`
- `workspace`

