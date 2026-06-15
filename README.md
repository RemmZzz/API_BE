# API_BE

Backend API cho nen tang API_FE, xay dung bang Java 21 va Spring Boot 3.5.x. Du an quan ly cac domain chinh gom user, authentication, project, database designer, API collection, environment, API tester, documentation/mock server, payment/subscription va admin dashboard.

## Trang thai hien tai

- Da co cau truc module, entity, DTO, repository, service va migration database cho cac domain chinh.
- Endpoint da expose day du hien tai nam o module `admin`: `/api/admin/**`.
- Cac controller `auth`, `user`, `project`, `database` dang co base path va se can bo sung method endpoint truoc khi public API cho frontend.
- Security/JWT/CORS/OpenAPI dang duoc scaffold; can hoan thien truoc khi chay production cong khai.

## Tech stack

| Thanh phan | Cong nghe |
| --- | --- |
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build tool | Maven Wrapper |
| API | Spring Web, Spring Validation |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8 |
| Migration | Flyway |
| Security | Spring Security, BCrypt |
| Mail/HTTP | Spring Mail, WebClient |
| Observability | Spring Boot Actuator |
| Local infra | Docker Compose |

## Yeu cau moi truong

- JDK 21
- Docker Desktop hoac MySQL 8 local
- Git
- PowerShell tren Windows hoac shell tuong duong tren Linux/macOS

Kiem tra nhanh:

```bash
java -version
docker --version
```

## Cau truc thu muc

```text
API_BE/
|-- compose.yaml
|-- docs/
|   |-- api-design.md
|   |-- database-design.md
|   `-- postman-collection.json
|-- scripts/
|   `-- scaffold-api-be.ps1
|-- src/
|   |-- main/
|   |   |-- java/com/apibe/API_BE/
|   |   |   |-- global/          # config, security, exception, response, util
|   |   |   |-- infrastructure/  # ai, email, http client, storage
|   |   |   `-- module/          # admin, auth, user, project, database, payment, ...
|   |   `-- resources/
|   |       |-- application.yml
|   |       |-- application-dev.yml
|   |       |-- application-prod.yml
|   |       `-- db/migration/
|   `-- test/
`-- pom.xml
```

## Chay local

Ung dung mac dinh dung profile `dev` va ket noi MySQL tai `localhost:3307`.

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

3. Kiem tra ung dung:

```text
http://localhost:8080
```

Health endpoint cua Actuator:

```text
GET http://localhost:8080/actuator/health
```

## Cau hinh

Profile mac dinh duoc dat trong `src/main/resources/application.yml`:

```yaml
spring:
  profiles:
    active: dev
```

### Profile dev

`application-dev.yml` dung MySQL local:

| Bien | Gia tri local |
| --- | --- |
| Database | `api_ai_db` |
| Host | `localhost` |
| Port | `3307` |
| Username | `api_user` |
| Password | `123123` |

Thong tin nay chi dung cho development. Khong su dung credential trong `compose.yaml` cho production.

### Profile prod

Khi chay production, dung profile `prod` va truyen bien moi truong:

```env
DB_HOST=your-db-host
DB_PORT=3306
DB_NAME=api_ai_db
DB_USERNAME=your-db-user
DB_PASSWORD=your-strong-password
```

Lenh chay voi profile production:

```bash
java -jar target/API_BE-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Hoac dung bien moi truong:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/API_BE-0.0.1-SNAPSHOT.jar
```

## Database migration

Flyway migration nam tai:

```text
src/main/resources/db/migration
```

Danh sach migration hien tai:

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
V10__fix_missing_admin_required_tables.sql
```

Quy tac production:

- Khong sua noi dung migration da deploy.
- Tao file `V{next}__description.sql` cho moi thay doi schema.
- Backup database truoc khi deploy migration len moi truong that.
- Giu `spring.jpa.hibernate.ddl-auto=validate`; khong dung `update` trong production.

## Build va test

Chay test:

```bash
./mvnw test
```

Build artifact:

```bash
./mvnw clean package
```

Artifact sau build:

```text
target/API_BE-0.0.1-SNAPSHOT.jar
```

Tren Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

## API surface

Base URL local:

```text
http://localhost:8080
```

Endpoint admin da expose:

| Method | Path | Mo ta |
| --- | --- | --- |
| `GET` | `/api/admin/overview` | Tong quan he thong |
| `GET` | `/api/admin/users` | Danh sach user, co filter/sort/pagination |
| `PATCH` | `/api/admin/users/{userId}/status` | Cap nhat trang thai user |
| `PATCH` | `/api/admin/users/{userId}/role` | Cap nhat role user |
| `GET` | `/api/admin/revenue` | Bao cao doanh thu |

Base path da scaffold:

```text
/api/auth
/api/user
/api/project
/api/database
```

Tai lieu bo sung:

- `docs/api-design.md`
- `docs/database-design.md`
- `docs/postman-collection.json`

## Production deployment checklist

Truoc khi dua len production, can hoan thien va xac nhan cac muc sau:

- Bat profile `prod` va cau hinh DB bang bien moi truong.
- Dung tai khoan database rieng cho ung dung, khong dung `root`.
- Doi toan bo password mau trong `compose.yaml`, `.env.example`, `application-dev.yml`.
- Hoan thien Security filter chain, JWT provider va rule public/protected endpoint.
- Cau hinh CORS theo domain frontend that.
- Cau hinh SMTP/Email neu dung OTP hoac reset password.
- Cau hinh logging, monitoring va alerting cho `/actuator/health`.
- Chay `./mvnw test` va `./mvnw clean package` trong CI.
- Backup database truoc moi dot deploy co migration.

## Lenh van hanh huu ich

Xem log MySQL local:

```bash
docker compose logs -f mysql
```

Dung MySQL local:

```bash
docker compose down
```

Dung va xoa volume local:

```bash
docker compose down -v
```

Chay ung dung voi profile cu the:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Bao mat

- Khong commit file `.env` hoac secret that vao Git.
- Khong tai su dung credential development cho staging/production.
- Khong expose Actuator endpoint nhay cam neu chua co auth va network restriction.
- Tat `show-sql` trong production.
- Kiem tra lai authorization cho moi endpoint admin va endpoint thao tac du lieu nguoi dung.
