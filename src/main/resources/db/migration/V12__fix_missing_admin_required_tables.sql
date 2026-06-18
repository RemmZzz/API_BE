CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) NOT NULL,
    username VARCHAR(255),
    email VARCHAR(255),
    name VARCHAR(255),
    password_hash VARCHAR(255),
    role VARCHAR(255),
    status VARCHAR(255),
    phone VARCHAR(255),
    avatar_url VARCHAR(255),
    last_login_at DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS otp_verifications (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_settings (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS api_keys (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS projects (
    id CHAR(36) NOT NULL,
    owner_id CHAR(36),
    name VARCHAR(255),
    status VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project_members (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS database_schemas (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS database_tables (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS database_columns (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS database_relationships (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS database_indexes (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS api_test_histories (
    id CHAR(36) NOT NULL,
    project_id CHAR(36),
    user_id CHAR(36),
    method VARCHAR(255),
    url VARCHAR(255),
    response_status INT,
    duration_ms BIGINT,
    response_size_bytes BIGINT,
    created_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ai_conversations (
    id CHAR(36) NOT NULL,
    project_id CHAR(36),
    user_id CHAR(36),
    title VARCHAR(255),
    mode VARCHAR(255),
    is_pinned BIT(1),
    is_archived BIT(1),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS payments (
    id CHAR(36) NOT NULL,
    user_id CHAR(36),
    subscription_id CHAR(36),
    order_code VARCHAR(255),
    provider VARCHAR(255),
    plan_id VARCHAR(255),
    plan_name VARCHAR(255),
    cycle VARCHAR(255),
    amount DECIMAL(38, 2),
    currency VARCHAR(255),
    status VARCHAR(255),
    created_at DATETIME(6),
    expired_at DATETIME(6),
    paid_at DATETIME(6),
    cancelled_at DATETIME(6),
    failed_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id CHAR(36) NOT NULL,
    user_id CHAR(36),
    project_id CHAR(36),
    module VARCHAR(255),
    category VARCHAR(255),
    action VARCHAR(255),
    description VARCHAR(255),
    status VARCHAR(255),
    metadata_json TEXT,
    is_hidden BIT(1),
    created_at DATETIME(6),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id CHAR(36) NOT NULL,
    admin_id CHAR(36),
    target_user_id CHAR(36),
    action VARCHAR(255),
    description VARCHAR(255),
    metadata_json TEXT,
    created_at DATETIME(6),
    PRIMARY KEY (id)
);
