# API_BE

Backend cho nen tang API_FE, duoc xay dung bang Java 21 va Spring Boot. Du an tap trung vao cac chuc nang quan ly nguoi dung, xac thuc, du an, thiet ke database, API collection, environment, API tester, documentation/mock server, payment va subscription.

## Cong nghe

- Java 21
- Spring Boot 3.5.14
- Maven Wrapper
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- Spring Actuator
- MySQL 8
- Flyway
- Lombok
- Docker Compose

## Yeu cau

- JDK 21
- Docker Desktop hoac MySQL 8 local
- Git

## Cau truc du an

```text
.
|-- compose.yaml                         # MySQL local bang Docker Compose
|-- docs/                                # Tai lieu thiet ke va Postman collection
|-- scripts/                             # Script scaffold/ho tro
|-- src/main/java/com/apibe/API_BE
|   |-- global/                          # Config, exception, response, security, util
|   |-- infrastructure/                  # AI, email, HTTP client, storage
|   `-- module/                          # Cac module nghiep vu
|       |-- auth/
|       |-- database/
|       |-- project/
|       `-- user/
`-- src/main/resources
    |-- application.yml
    |-- application-dev.yml
    |-- application-prod.yml
    `-- db/migration/                    # Flyway migrations
```

## Module hien co

- `auth`: dang ky, dang nhap, OTP va reset mat khau.
- `user`: ho so nguoi dung, cai dat tai khoan, phien dang nhap va API key.
- `project`: quan ly du an va thanh vien du an.
- `database`: schema, table, column, relationship, index va SQL preview.
- Cac module dang scaffold: `activity`, `admin`, `apitester`, `collection`, `documentation`, `environment`, `mockserver`, `payment`, `subscription`, `workspace`.

## Chay local

Mac dinh ung dung dung profile `dev` va ket noi MySQL tai `localhost:3307`.

1. Khoi dong MySQL:

```bash
docker compose up -d
```

2. Chay ung dung:

```bash
./mvnw spring-boot:run
```

Tren Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Ung dung se chay tai:

```text
http://localhost:8080
```

## Kiem thu va build

Chay test:

```bash
./mvnw test
```

Build package:

```bash
./mvnw clean package
```

## Cau hinh

Profile mac dinh:

```yaml
spring:
  profiles:
    active: dev
```

Profile `dev` dung cau hinh MySQL local trong `compose.yaml`:

```text
DB: api_ai_db
User: api_user
Port: 3307
```

Profile `prod` doc bien moi truong:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

## Database migration

Flyway migration nam trong:

```text
src/main/resources/db/migration
```

Thu tu migration hien tai:

- `V1__init_users.sql`
- `V2__init_projects.sql`
- `V3__init_database_designer.sql`
- `V4__init_api_collections.sql`
- `V5__init_environments.sql`
- `V6__init_api_tester.sql`
- `V7__init_documentation_mock.sql`
- `V8__init_payment_subscription.sql`
- `V9__add_indexes.sql`

## API base paths

Controller hien da scaffold cac base path:

```text
/api/auth
/api/user
/api/project
/api/database
```

Chi tiet request/response se duoc cap nhat khi cac endpoint duoc hoan thien.

## Tai lieu

- `docs/api-design.md`
- `docs/database-design.md`
- `docs/postman-collection.json`

## Ghi chu bao mat

Cac mat khau trong `compose.yaml`, `.env.example` va `application-dev.yml` chi nen dung cho moi truong local/dev. Khong su dung cac gia tri nay cho production.
