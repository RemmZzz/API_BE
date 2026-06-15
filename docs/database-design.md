# Database Design

## Database Schema (DDL)

Dưới đây là cấu trúc cơ sở dữ liệu chi tiết của hệ thống `api_ai_db` được sử dụng làm tham chiếu thiết kế chính cho dự án:

```sql
CREATE DATABASE api_ai_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE api_ai_db;

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100),
    password_hash TEXT NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED', 'PENDING') NOT NULL DEFAULT 'PENDING',
    phone VARCHAR(20),
    avatar_url TEXT,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_sessions (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    refresh_token_hash TEXT NOT NULL,
    ip_address VARCHAR(100),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE otp_verifications (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36),
    email VARCHAR(255) NOT NULL,
    otp_hash TEXT NOT NULL,
    purpose VARCHAR(50) NOT NULL DEFAULT 'REGISTER',
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_otp_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    token_hash TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE user_settings (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL UNIQUE,
    language VARCHAR(10) NOT NULL DEFAULT 'vi',
    notification_settings JSON,
    privacy_settings JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_settings_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE api_keys (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    key_hash TEXT NOT NULL,
    prefix VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    last_used_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_keys_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE projects (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    owner_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    type VARCHAR(50),
    status ENUM('ACTIVE', 'ARCHIVED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    color VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE project_members (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    role ENUM('OWNER', 'ADMIN', 'MEMBER', 'VIEWER') NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(project_id, user_id),

    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_project_members_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE workspaces (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL UNIQUE,
    name VARCHAR(150),
    config_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_workspaces_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE TABLE ai_conversations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    user_id CHAR(36),
    title VARCHAR(200) NOT NULL,
    mode VARCHAR(50) NOT NULL DEFAULT 'chat',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_conversations_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_conversations_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE TABLE ai_messages (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    conversation_id CHAR(36) NOT NULL,
    role ENUM('user', 'assistant', 'system') NOT NULL,
    content LONGTEXT NOT NULL,
    metadata_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id)
        ON DELETE CASCADE
);

CREATE TABLE database_schemas (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    db_type VARCHAR(50) NOT NULL DEFAULT 'mysql',
    name VARCHAR(150) DEFAULT 'default_schema',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE(project_id, name),

    CONSTRAINT fk_database_schemas_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE TABLE database_tables (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    schema_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    display_name VARCHAR(150),
    row_count INT NOT NULL DEFAULT 0,
    position_x INT DEFAULT 0,
    position_y INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE(schema_id, name),

    CONSTRAINT fk_database_tables_schema
        FOREIGN KEY (schema_id) REFERENCES database_schemas(id)
        ON DELETE CASCADE
);

CREATE TABLE database_columns (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    table_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    data_type VARCHAR(100) NOT NULL,
    length INT,
    precision_value INT,
    scale_value INT,
    is_primary_key BOOLEAN NOT NULL DEFAULT FALSE,
    is_nullable BOOLEAN NOT NULL DEFAULT TRUE,
    is_unique BOOLEAN NOT NULL DEFAULT FALSE,
    is_auto_increment BOOLEAN NOT NULL DEFAULT FALSE,
    default_value TEXT,
    ordinal_position INT NOT NULL DEFAULT 0,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE(table_id, name),

    CONSTRAINT fk_database_columns_table
        FOREIGN KEY (table_id) REFERENCES database_tables(id)
        ON DELETE CASCADE
);

CREATE TABLE database_relationships (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    schema_id CHAR(36) NOT NULL,
    source_table_id CHAR(36) NOT NULL,
    source_column_id CHAR(36) NOT NULL,
    target_table_id CHAR(36) NOT NULL,
    target_column_id CHAR(36) NOT NULL,
    constraint_name VARCHAR(150),
    on_delete_action VARCHAR(50) DEFAULT 'NO ACTION',
    on_update_action VARCHAR(50) DEFAULT 'NO ACTION',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_relationships_schema
        FOREIGN KEY (schema_id) REFERENCES database_schemas(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_relationships_source_table
        FOREIGN KEY (source_table_id) REFERENCES database_tables(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_relationships_source_column
        FOREIGN KEY (source_column_id) REFERENCES database_columns(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_relationships_target_table
        FOREIGN KEY (target_table_id) REFERENCES database_tables(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_relationships_target_column
        FOREIGN KEY (target_column_id) REFERENCES database_columns(id)
        ON DELETE CASCADE
);

CREATE TABLE database_indexes (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    table_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    columns_json JSON NOT NULL,
    is_unique BOOLEAN NOT NULL DEFAULT FALSE,
    index_type VARCHAR(50) DEFAULT 'BTREE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(table_id, name),

    CONSTRAINT fk_database_indexes_table
        FOREIGN KEY (table_id) REFERENCES database_tables(id)
        ON DELETE CASCADE
);

CREATE TABLE api_collections (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_collections_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE TABLE api_folders (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    collection_id CHAR(36) NOT NULL,
    parent_folder_id CHAR(36),
    name VARCHAR(150) NOT NULL,
    ordinal_position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_folders_collection
        FOREIGN KEY (collection_id) REFERENCES api_collections(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_api_folders_parent
        FOREIGN KEY (parent_folder_id) REFERENCES api_folders(id)
        ON DELETE CASCADE
);

CREATE TABLE api_requests (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    collection_id CHAR(36) NOT NULL,
    folder_id CHAR(36),
    name VARCHAR(150) NOT NULL,
    method ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE') NOT NULL DEFAULT 'GET',
    url TEXT NOT NULL,
    description TEXT,
    headers_json JSON,
    params_json JSON,
    body LONGTEXT,
    body_type VARCHAR(50) DEFAULT 'json',
    response_example LONGTEXT,
    ordinal_position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_requests_collection
        FOREIGN KEY (collection_id) REFERENCES api_collections(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_api_requests_folder
        FOREIGN KEY (folder_id) REFERENCES api_folders(id)
        ON DELETE SET NULL
);

CREATE TABLE environments (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE(project_id, name),

    CONSTRAINT fk_environments_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE TABLE environment_variables (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    environment_id CHAR(36) NOT NULL,
    variable_key VARCHAR(100) NOT NULL,
    initial_value TEXT,
    current_value TEXT,
    type VARCHAR(50) DEFAULT 'default',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_secret BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE(environment_id, variable_key),

    CONSTRAINT fk_environment_variables_environment
        FOREIGN KEY (environment_id) REFERENCES environments(id)
        ON DELETE CASCADE
);

CREATE TABLE active_environments (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL UNIQUE,
    environment_id CHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_active_environments_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_active_environments_environment
        FOREIGN KEY (environment_id) REFERENCES environments(id)
        ON DELETE SET NULL
);

CREATE TABLE api_test_histories (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36),
    user_id CHAR(36),
    method ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE') NOT NULL,
    url TEXT NOT NULL,
    request_headers_json JSON,
    request_body LONGTEXT,
    response_status INT,
    response_status_text VARCHAR(100),
    response_headers_json JSON,
    response_body LONGTEXT,
    duration_ms INT,
    response_size_bytes INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_test_histories_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_api_test_histories_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE TABLE api_documentations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    version VARCHAR(50) DEFAULT '1.0.0',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_documentations_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE TABLE api_documentation_endpoints (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    documentation_id CHAR(36) NOT NULL,
    method ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE') NOT NULL,
    url TEXT NOT NULL,
    description TEXT,
    headers_json JSON,
    params_json JSON,
    body_example LONGTEXT,
    response_example LONGTEXT,
    error_example LONGTEXT,
    ordinal_position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_doc_endpoints_documentation
        FOREIGN KEY (documentation_id) REFERENCES api_documentations(id)
        ON DELETE CASCADE
);

CREATE TABLE mock_endpoints (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    method ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE') NOT NULL,
    path TEXT NOT NULL,
    status_code INT NOT NULL DEFAULT 200,
    delay_ms INT NOT NULL DEFAULT 0,
    response_headers_json JSON,
    response_body LONGTEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CHECK (status_code >= 100 AND status_code <= 599),

    CONSTRAINT fk_mock_endpoints_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE TABLE subscription_plans (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price_monthly DECIMAL(12, 2) NOT NULL DEFAULT 0,
    price_yearly DECIMAL(12, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    limits_json JSON,
    features_json JSON,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscriptions (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    plan_id CHAR(36) NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    cycle VARCHAR(30) NOT NULL DEFAULT 'monthly',
    status ENUM('ACTIVE', 'INACTIVE', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    started_at TIMESTAMP NULL,
    activated_at TIMESTAMP NULL,
    expired_at TIMESTAMP NULL,
    payment_order_code VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_subscriptions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_subscriptions_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);

CREATE TABLE payments (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    subscription_id CHAR(36),
    order_code VARCHAR(100) NOT NULL UNIQUE,
    provider VARCHAR(50) DEFAULT 'BANK_TRANSFER',
    bank_name VARCHAR(100),
    account_name VARCHAR(150),
    account_number VARCHAR(100),
    transfer_content TEXT,
    plan_id CHAR(36),
    plan_name VARCHAR(100),
    cycle VARCHAR(30),
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    status ENUM('PENDING', 'PAID', 'SUCCESS', 'FAILED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP NULL,
    paid_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    failed_at TIMESTAMP NULL,

    CONSTRAINT fk_payments_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_payments_subscription
        FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_payments_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
        ON DELETE SET NULL
);

CREATE TABLE payment_events (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    payment_id CHAR(36) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_events_payment
        FOREIGN KEY (payment_id) REFERENCES payments(id)
        ON DELETE CASCADE
);

CREATE TABLE activity_logs (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36),
    project_id CHAR(36),
    module VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'SUCCESS',
    metadata_json JSON,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_logs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_activity_logs_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON DELETE CASCADE
);

CREATE TABLE admin_audit_logs (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    admin_id CHAR(36),
    target_user_id CHAR(36),
    action VARCHAR(100) NOT NULL,
    description TEXT,
    metadata_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_admin_audit_logs_admin
        FOREIGN KEY (admin_id) REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_admin_audit_logs_target_user
        FOREIGN KEY (target_user_id) REFERENCES users(id)
        ON DELETE SET NULL
);

-- Indexes for MySQL
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);

CREATE INDEX idx_ai_conversations_project_id ON ai_conversations(project_id);
CREATE INDEX idx_ai_messages_conversation_id ON ai_messages(conversation_id);

CREATE INDEX idx_database_schemas_project_id ON database_schemas(project_id);
CREATE INDEX idx_database_tables_schema_id ON database_tables(schema_id);
CREATE INDEX idx_database_columns_table_id ON database_columns(table_id);

CREATE INDEX idx_api_collections_project_id ON api_collections(project_id);
CREATE INDEX idx_api_requests_collection_id ON api_requests(collection_id);
CREATE INDEX idx_environments_project_id ON environments(project_id);
CREATE INDEX idx_environment_variables_environment_id ON environment_variables(environment_id);

CREATE INDEX idx_api_test_histories_project_id ON api_test_histories(project_id);
CREATE INDEX idx_api_test_histories_user_id ON api_test_histories(user_id);
CREATE INDEX idx_api_test_histories_created_at ON api_test_histories(created_at);

CREATE INDEX idx_mock_endpoints_project_id ON mock_endpoints(project_id);

CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_project_id ON activity_logs(project_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);
```
