# Cau truc du an va chuc nang

Tai lieu nay tom tat cau truc thu muc, cong nghe chinh va cac chuc nang dang co trong du an `API_BE`.

## Tong quan

`API_BE` la backend cho nen tang API_FE, xay dung bang Java 21 va Spring Boot. Du an huong toi cac nhom chuc nang: xac thuc nguoi dung, quan ly user/profile, quan ly project, thiet ke database, quan ly API collection, API tester, documentation/mock server, payment va subscription.

## Cong nghe chinh

- Java 21
- Spring Boot 3.5.14
- Maven Wrapper
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- Spring Actuator
- OAuth2 Client
- MySQL 8
- Flyway
- Lombok
- Docker Compose
- H2 cho test

## Cau truc thu muc

```text
.
|-- .mvn/                               # Cau hinh Maven Wrapper
|-- docs/                               # Tai lieu thiet ke va Postman collection
|   |-- api-design.md
|   |-- database-design.md
|   `-- postman-collection.json
|-- scripts/                            # Script scaffold/ho tro phat trien
|   `-- scaffold-api-be.ps1
|-- src/
|   |-- main/
|   |   |-- java/com/apibe/API_BE/
|   |   |   |-- ApiFeBackendApplication.java
|   |   |   |-- global/                  # Thanh phan dung chung toan he thong
|   |   |   |   |-- config/              # CORS, OpenAPI, Security, WebClient
|   |   |   |   |-- enums/               # Enum dung chung
|   |   |   |   |-- exception/           # Xu ly loi tap trung
|   |   |   |   |-- response/            # ApiResponse, ErrorResponse, PageResponse
|   |   |   |   |-- security/            # JWT, user details, filter security
|   |   |   |   `-- util/                # Tien ich date/time, json, password, validation
|   |   |   |-- infrastructure/          # Tich hop ha tang ben ngoai
|   |   |   |   |-- ai/                  # AI client/mock AI client
|   |   |   |   |-- email/               # Email service SMTP
|   |   |   |   |-- http/                # HTTP client service
|   |   |   |   `-- storage/             # Luu tru file local
|   |   |   `-- module/                  # Module nghiep vu
|   |   |       |-- apitester/
|   |   |       |-- auth/
|   |   |       |-- collection/
|   |   |       |-- database/
|   |   |       |-- project/
|   |   |       `-- user/
|   |   `-- resources/
|   |       |-- application.yml
|   |       |-- application-dev.yml
|   |       |-- application-prod.yml
|   |       |-- db/migration/            # Flyway migration SQL
|   |       `-- templates/email/         # Template email OTP/reset password
|   `-- test/
|       |-- java/                        # Test Java
|       `-- resources/                   # Cau hinh test
|-- compose.yaml                         # MySQL local bang Docker Compose
|-- docker-compose.backup.yaml
|-- pom.xml                              # Cau hinh Maven va dependency
|-- README.md
|-- HELP.md
|-- mvnw
`-- mvnw.cmd
```

## Cau truc module backend

Moi module nghiep vu thuong duoc to chuc theo cac lop:

- `controller`: khai bao REST API endpoint.
- `service`: xu ly nghiep vu.
- `repository`: truy van database qua Spring Data JPA.
- `entity`: mapping bang database.
- `dto/request`: du lieu dau vao cua API.
- `dto/response`: du lieu tra ve cua API.
- `mapper`: chuyen doi entity sang DTO hoac nguoc lai.

## Cac module va chuc nang

### 1. `auth`

Muc dich: xu ly xac thuc va vong doi tai khoan.

Thanh phan hien co:

- `AuthController`: scaffold base path `/api/auth`.
- `AuthService`: service xac thuc.
- `OtpService`: xu ly OTP.
- `PasswordResetService`: xu ly quen mat khau/dat lai mat khau.
- DTO request/response cho dang ky, dang nhap, verify OTP, forgot password, reset password va token.
- Template email:
  - `otp-email.html`
  - `reset-password-email.html`

Ghi chu: controller hien moi khai bao base path, chua co endpoint chi tiet trong code.

### 2. `user`

Muc dich: quan ly thong tin nguoi dung, profile, cai dat, session va API key.

Thanh phan hien co:

- `UserController`: scaffold base path `/api/user`.
- `ProfileController`: scaffold base path `/api/user`.
- `UserService`, `ProfileService`.
- Entity: `User`, `UserSetting`, `UserSession`, `ApiKey`, `OtpVerification`, `PasswordResetToken`.
- Repository cho user, session, OTP va password reset token.
- DTO cho cap nhat profile, doi mat khau va cai dat nguoi dung.

Ghi chu: controller hien moi khai bao base path, chua co endpoint chi tiet trong code.

### 3. `project`

Muc dich: quan ly project va thanh vien trong project.

Thanh phan hien co:

- `ProjectController`: scaffold base path `/api/project`.
- `ProjectService`, `ProjectPermissionService`.
- Entity: `Project`, `ProjectMember`.
- Repository: `ProjectRepository`, `ProjectMemberRepository`.
- DTO tao/cap nhat project va response chi tiet project.
- Enum lien quan: `ProjectStatus`, `MemberRole`.

Ghi chu: controller hien moi khai bao base path, chua co endpoint chi tiet trong code.

### 4. `database`

Muc dich: ho tro thiet ke database/schema cho project.

Thanh phan hien co:

- Controller scaffold base path `/api/database`:
  - `DatabaseSchemaController`
  - `DatabaseTableController`
  - `DatabaseColumnController`
  - `DatabaseRelationshipController`
- Service:
  - `DatabaseSchemaService`
  - `DatabaseTableService`
  - `DatabaseColumnService`
  - `DatabaseRelationshipService`
  - `SqlGeneratorService`
- Entity:
  - `DatabaseSchema`
  - `DatabaseTable`
  - `DatabaseColumn`
  - `DatabaseRelationship`
  - `DatabaseIndex`
- DTO tao/cap nhat schema, table, column, relationship va preview SQL.

Ghi chu: controller hien moi khai bao base path, chua co endpoint chi tiet trong code.

### 5. `collection`

Muc dich: quan ly API collection, folder va request trong project.

Thanh phan hien co:

- `CollectionController`
- `ApiRequestController`
- `CollectionService`
- Entity: `Collection`, `CollectionFolder`, `ApiRequest`.
- Repository cho collection, folder va request.
- DTO tao collection, folder, API request va cap nhat API request.

Endpoint hien co:

| Method | Path | Chuc nang |
| --- | --- | --- |
| `GET` | `/api/projects/{projectId}/collections` | Lay danh sach collection theo project |
| `POST` | `/api/projects/{projectId}/collections` | Tao collection moi trong project |
| `POST` | `/api/collections/{collectionId}/folders` | Tao folder trong collection |
| `POST` | `/api/collections/{collectionId}/requests` | Tao API request trong collection |
| `PATCH` | `/api/requests/{requestId}` | Cap nhat API request |

### 6. `apitester`

Muc dich: gui request HTTP tu server, do thoi gian phan hoi va luu lich su test API.

Thanh phan hien co:

- `ApiTesterController`
- `ApiTesterService`
- `ApiTestHistoryRepository`
- Entity: `ApiTestHistory`
- DTO request/response cho gui API va lich su test.

Chuc nang noi bat:

- Gui HTTP request voi method, URL, header, query params va body.
- Ho tro timeout toi da 60 giay.
- Chan cac URL loopback/private/internal de giam rui ro SSRF.
- Ghi nhan status code, status text, response headers, response body, duration va error message.
- Co tuy chon luu lich su test.
- Lay lich su theo project, loc theo method/keyword, phan trang va sap xep.
- Xoa mot ban ghi lich su test.

Endpoint hien co:

| Method | Path | Chuc nang |
| --- | --- | --- |
| `POST` | `/api/api-tester/send` | Gui request HTTP va tra ve ket qua |
| `GET` | `/api/projects/{projectId}/api-test-history` | Lay lich su test API theo project |
| `DELETE` | `/api/test-history/{historyId}` | Xoa lich su test API |

## Thanh phan global

### `global/config`

- `CorsConfig`: cau hinh CORS.
- `OpenApiConfig`: cau hinh OpenAPI/Swagger.
- `SecurityConfig`: cau hinh Spring Security, hien tai tat CSRF va cho phep tat ca request.
- `WebClientConfig`: cau hinh WebClient/HTTP client.

### `global/security`

- `JwtTokenProvider`: xu ly JWT.
- `JwtAuthenticationFilter`: filter xac thuc JWT.
- `CustomUserDetails`, `CustomUserDetailsService`: tich hop user voi Spring Security.
- `SecurityUtils`: tien ich lay thong tin bao mat hien tai.

### `global/exception`

- `AppException`: exception nghiep vu.
- `ErrorCode`: ma loi dung chung.
- `GlobalExceptionHandler`: xu ly loi tap trung va tra ve response thong nhat.

### `global/response`

- `ApiResponse`: response thanh cong/thong thuong.
- `ErrorResponse`: response loi.
- `PageResponse`: response phan trang.

## Infrastructure

- `infrastructure/ai`: client AI va mock AI client.
- `infrastructure/email`: service gui email qua SMTP.
- `infrastructure/http`: service goi HTTP ben ngoai.
- `infrastructure/storage`: service luu tru file local.

## Database va migration

Du an dung Flyway, migration nam tai:

```text
src/main/resources/db/migration
```

Cac nhom bang chinh:

- User/auth:
  - `users`
  - `api_key`
  - `otp_verification`
  - `password_reset_token`
  - `user_session`
  - `user_setting`
- Project:
  - `projects`
  - `project_members`
- Database designer:
  - `database_schema`
  - `database_table`
  - `database_column`
  - `database_relationship`
  - `database_index`
- API collection:
  - `collections`
  - `collection_folders`
  - `api_requests`
- API tester:
  - `api_test_history`

Migration hien co:

```text
V1__init_users.sql
V2__init_projects.sql
V3__init_database_designer.sql
V4__init_api_collections.sql
V5__init_environments.sql
V6__init_api_tester.sql
V7__init_documentation_mock.sql
V8__init_payment_subscription.sql
V9__add_indexes.sql
V10__create_api_test_history.sql
```

## Cau hinh chay local

Ung dung chay cong mac dinh:

```text
http://localhost:8080
```

Profile mac dinh:

```yaml
spring:
  profiles:
    active: dev
```

Profile `dev` ket noi MySQL local:

```text
Host: localhost
Port: 3307
Database: api_ai_db
Username: api_user
```

Docker Compose tao MySQL container:

```text
Container: api_ai_mysql
Image: mysql:8.0
Port mapping: 3307:3306
Volume: mysql_data
```

## Lenh hay dung

Chay MySQL local:

```powershell
docker compose up -d
```

Chay ung dung tren Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Chay test:

```powershell
.\mvnw.cmd test
```

Build package:

```powershell
.\mvnw.cmd clean package
```

## Ghi chu hien trang

- `collection` va `apitester` da co endpoint cu the trong controller.
- `auth`, `user`, `project` va `database` da co cau truc module, DTO/entity/repository/service, nhung controller hien chu yeu moi scaffold base path.
- `docs/api-design.md` va `docs/database-design.md` hien moi co tieu de, chua co noi dung chi tiet.
- Security hien tai dang `permitAll`; can that chat quyen truy cap khi hoan thien auth/authorization.
