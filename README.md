# API_BE

Backend API cho nền tảng API_FE, xây dựng bằng Java 21 và Spring Boot 3.5.x. Dự án quản lý các domain chính gồm user, authentication, project, database designer, API collection, environment, API tester, documentation/mock server, payment/subscription và admin dashboard.

## Trạng thái hiện tại

- Đã có cấu trúc module, entity, DTO, repository, service và migration database cho các domain chính.
- Endpoint đã expose đầy đủ hiện tại nằm ở module `admin`: `/api/admin/**`.
- Các controller `auth`, `user`, `project`, `database` đang có base path và đã được bổ sung đầy đủ các endpoint tương thích với frontend.
- Cấu hình Security/JWT/CORS/OpenAPI đã hoàn thiện và sẵn sàng vận hành.

## Nhật ký thay đổi gần đây (Changelog)

Vừa cập nhật chuỗi sửa lỗi Bảo mật và Tối ưu hóa Backend (Toàn bộ 51/51 tests đều PASS):
* **Bảo mật & CORS**: Chuyển CORS sang đọc động từ biến môi trường (`app.cors.allowed-origins`).
* **Tránh rò rỉ JWT**: Triển khai mã trao đổi một lần (`exchangeCode`) ngắn hạn sau khi đăng nhập OAuth2 thành công, thay vì đính JWT trực tiếp lên URL redirect.
* **Rate Limiting OTP**: Tích hợp **Bucket4j** giới hạn tần suất gửi OTP theo địa chỉ IP của client (tối đa 3 OTP, refill 1 token/60s).
* **Tối ưu DB (N+1 queries)**:
  - Lấy dự án: Dùng `@EntityGraph` (nạp trước `workspace` và `owner`), giảm từ $N+1$ xuống còn 1 query.
  - Database Schema: Dùng lệnh `IN` truy vấn cột hàng loạt (`findByTableIdIn`), giảm từ $N+1$ xuống còn 2 queries.
* **SePay Webhook**: Thêm đồng bộ hóa tránh race condition, trích xuất mã hóa đơn bằng Regex, sinh mã kèm hậu tố ngẫu nhiên tránh trùng lặp.
* **Cấu hình & Exception**: Chuyển Gemini RestClient thành Bean dùng chung và ẩn chi tiết stacktrace của lỗi hệ thống unhandled ở môi trường `production`.
* **Quản lý Secrets**: Chuyển cấu hình nhạy cảm sang biến môi trường trong `application-dev.yml` và cung cấp `.env.example`.

## Tech stack

| Thành phần | Công nghệ |
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

## Yêu cầu môi trường

- JDK 21
- Docker Desktop hoặc MySQL 8 local
- Git
- PowerShell trên Windows hoặc shell tương đương trên Linux/macOS

Kiểm tra nhanh:

```bash
java -version
docker --version
```

## Cấu trúc thư mục

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
|`-- pom.xml
```

## Chạy local

Ứng dụng mặc định dùng profile `dev` và kết nối MySQL tại `localhost:3307`.

1. Khởi động MySQL:

```bash
docker compose up -d
```

2. Chạy ứng dụng:

```bash
./mvnw spring-boot:run
```

Trên Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

3. Kiểm tra ứng dụng:

```text
http://localhost:8080
```

Health endpoint của Actuator:

```text
GET http://localhost:8080/actuator/health
```

## Cấu hình

Profile mặc định được đặt trong `src/main/resources/application.yml`:

```yaml
spring:
  profiles:
    active: dev
```

### Profile dev

`application-dev.yml` dùng MySQL local:

| Biến | Giá trị local |
| --- | --- |
| Database | `api_ai_db` |
| Host | `localhost` |
| Port | `3307` |
| Username | `api_user` |
| Password | `123123` |

Thông tin này chỉ dùng cho development. Không sử dụng credential trong `compose.yaml` cho production.

### Profile prod

Khi chạy production, dùng profile `prod` và truyền biến môi trường (Tham khảo `.env.example` để cấu hình):

```env
DB_HOST=your-db-host
DB_PORT=3306
DB_NAME=api_ai_db
DB_USERNAME=your-db-user
DB_PASSWORD=your-strong-password
```

Lệnh chạy với profile production:

```bash
java -jar target/API_BE-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Hoặc dùng biến môi trường:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar target/API_BE-0.0.1-SNAPSHOT.jar
```

## Database migration

Flyway migration nằm tại:

```text
src/main/resources/db/migration
```

Danh sách migration hiện tại:

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

Quy tắc production:

- Không sửa nội dung migration đã deploy.
- Tạo file `V{next}__description.sql` cho mỗi thay đổi schema.
- Backup database trước khi deploy migration lên môi trường thật.
- Giữ `spring.jpa.hibernate.ddl-auto=validate`; không dùng `update` trong production.

## Build và test

Chạy test:

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

Trên Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

## API surface

Base URL local:

```text
http://localhost:8080
```

Endpoint admin đã expose:

| Method | Path | Mô tả |
| --- | --- | --- |
| `GET` | `/api/admin/overview` | Tổng quan hệ thống |
| `GET` | `/api/admin/users` | Danh sách user, có filter/sort/pagination |
| `PATCH` | `/api/admin/users/{userId}/status` | Cập nhật trạng thái user |
| `PATCH` | `/api/admin/users/{userId}/role` | Cập nhật role user |
| `GET` | `/api/admin/revenue` | Báo cáo doanh thu |

Base path đã scaffold:

```text
/api/auth
/api/user
/api/project
/api/database
```

Tài liệu bổ sung:

- `docs/api-design.md`
- `docs/database-design.md`
- `docs/postman-collection.json`

## Production deployment checklist

Trước khi đưa lên production, cần hoàn thiện và xác nhận các mục sau:

- Bật profile `prod` và cấu hình DB bằng biến môi trường.
- Dùng tài khoản database riêng cho ứng dụng, không dùng `root`.
- Thay đổi toàn bộ password mẫu trong `compose.yaml`, `.env.example`, `application-dev.yml`.
- Hoàn thiện Security filter chain, JWT provider và rule public/protected endpoint.
- Cấu hình CORS theo domain frontend thật qua biến môi trường.
- Cấu hình SMTP/Email nếu dùng OTP hoặc reset password.
- Cấu hình logging, monitoring và alerting cho `/actuator/health`.
- Chạy `./mvnw test` và `./mvnw clean package` trong CI.
- Backup database trước mỗi đợt deploy có migration.

## Lệnh vận hành hữu ích

Xem log MySQL local:

```bash
docker compose logs -f mysql
```

Dừng MySQL local:

```bash
docker compose down
```

Dừng và xóa volume local:

```bash
docker compose down -v
```

Chạy ứng dụng với profile cụ thể:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Bảo mật

- Không commit file `.env` hoặc secret thật vào Git.
- Không tái sử dụng credential development cho staging/production.
- Không expose Actuator endpoint nhạy cảm nếu chưa có auth và network restriction.
- Tắt `show-sql` trong production.
- Kiểm tra lại authorization cho mỗi endpoint admin và endpoint thao tác dữ liệu người dùng.
