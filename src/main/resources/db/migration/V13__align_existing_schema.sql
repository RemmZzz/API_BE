-- Create new tables if they do not exist
CREATE TABLE IF NOT EXISTS subscription_plans (
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

CREATE TABLE IF NOT EXISTS subscriptions (
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
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id)
);

CREATE TABLE IF NOT EXISTS ai_messages (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    conversation_id CHAR(36) NOT NULL,
    role ENUM('user', 'assistant', 'system') NOT NULL,
    content LONGTEXT NOT NULL,
    metadata_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_messages_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE,
    INDEX idx_ai_messages_conversation_id (conversation_id)
);

CREATE TABLE IF NOT EXISTS mock_endpoints (
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
    CONSTRAINT fk_mock_endpoints_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_mock_endpoints_project_id (project_id)
);

-- Alter users table
ALTER TABLE users MODIFY COLUMN name VARCHAR(100);
ALTER TABLE users MODIFY COLUMN password_hash TEXT NOT NULL;
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20);
ALTER TABLE users MODIFY COLUMN avatar_url TEXT;
ALTER TABLE users MODIFY COLUMN last_login_at TIMESTAMP NULL;
ALTER TABLE users MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Alter user_sessions table
-- To prevent failure if columns already exist, we use a block or ignore errors (standard SQL alter)
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS user_id CHAR(36) NOT NULL AFTER id;
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS refresh_token_hash TEXT NOT NULL AFTER user_id;
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS ip_address VARCHAR(100) AFTER refresh_token_hash;
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS user_agent TEXT AFTER ip_address;
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL AFTER user_agent;
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL AFTER expires_at;
ALTER TABLE user_sessions MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE user_sessions DROP COLUMN IF EXISTS updated_at;
-- Add constraint if not exists (we drop first just in case to avoid duplicates)
ALTER TABLE user_sessions DROP FOREIGN KEY fk_user_sessions_user;
ALTER TABLE user_sessions ADD CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Alter otp_verifications table
ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS user_id CHAR(36) AFTER id;
ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS email VARCHAR(255) NOT NULL AFTER user_id;
ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS otp_hash TEXT NOT NULL AFTER email;
ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS purpose VARCHAR(50) NOT NULL DEFAULT 'REGISTER' AFTER otp_hash;
ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL AFTER purpose;
ALTER TABLE otp_verifications ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP NULL AFTER expires_at;
ALTER TABLE otp_verifications MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE otp_verifications DROP COLUMN IF EXISTS updated_at;
ALTER TABLE otp_verifications DROP FOREIGN KEY fk_otp_user;
ALTER TABLE otp_verifications ADD CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Alter password_reset_tokens table
ALTER TABLE password_reset_tokens ADD COLUMN IF NOT EXISTS user_id CHAR(36) NOT NULL AFTER id;
ALTER TABLE password_reset_tokens ADD COLUMN IF NOT EXISTS token_hash TEXT NOT NULL AFTER user_id;
ALTER TABLE password_reset_tokens ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP NOT NULL AFTER token_hash;
ALTER TABLE password_reset_tokens ADD COLUMN IF NOT EXISTS used_at TIMESTAMP NULL AFTER expires_at;
ALTER TABLE password_reset_tokens MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE password_reset_tokens DROP COLUMN IF EXISTS updated_at;
ALTER TABLE password_reset_tokens DROP FOREIGN KEY fk_password_reset_user;
ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Alter user_settings table
ALTER TABLE user_settings MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE user_settings MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Alter api_keys table
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS user_id CHAR(36) NOT NULL AFTER id;
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL AFTER user_id;
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS key_hash TEXT NOT NULL AFTER name;
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS prefix VARCHAR(20) NOT NULL AFTER key_hash;
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' AFTER prefix;
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS last_used_at TIMESTAMP NULL AFTER status;
ALTER TABLE api_keys ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP NULL AFTER last_used_at;
ALTER TABLE api_keys MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE api_keys DROP COLUMN IF EXISTS updated_at;
ALTER TABLE api_keys DROP FOREIGN KEY fk_api_keys_user;
ALTER TABLE api_keys ADD CONSTRAINT fk_api_keys_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Alter projects table
ALTER TABLE projects MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE projects MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Alter project_members table
ALTER TABLE project_members MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE project_members DROP COLUMN IF EXISTS updated_at;

-- Alter database_schemas table
ALTER TABLE database_schemas MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE database_schemas MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Alter database_tables table
ALTER TABLE database_tables MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE database_tables MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Alter database_columns table
ALTER TABLE database_columns MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE database_columns MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Alter database_relationships table
ALTER TABLE database_relationships MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE database_relationships DROP COLUMN IF EXISTS updated_at;

-- Alter database_indexes table
ALTER TABLE database_indexes MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE database_indexes DROP COLUMN IF EXISTS updated_at;

-- Alter api_test_histories table
ALTER TABLE api_test_histories MODIFY COLUMN method ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE') NOT NULL;
ALTER TABLE api_test_histories MODIFY COLUMN url TEXT NOT NULL;
ALTER TABLE api_test_histories ADD COLUMN IF NOT EXISTS request_headers_json JSON AFTER url;
ALTER TABLE api_test_histories ADD COLUMN IF NOT EXISTS request_body LONGTEXT AFTER request_headers_json;
ALTER TABLE api_test_histories ADD COLUMN IF NOT EXISTS response_status_text VARCHAR(100) AFTER response_status;
ALTER TABLE api_test_histories ADD COLUMN IF NOT EXISTS response_headers_json JSON AFTER response_status_text;
ALTER TABLE api_test_histories ADD COLUMN IF NOT EXISTS response_body LONGTEXT AFTER response_headers_json;
ALTER TABLE api_test_histories MODIFY COLUMN duration_ms INT;
ALTER TABLE api_test_histories MODIFY COLUMN response_size_bytes INT;
ALTER TABLE api_test_histories MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE api_test_histories DROP FOREIGN KEY fk_api_test_histories_project;
ALTER TABLE api_test_histories DROP FOREIGN KEY fk_api_test_histories_user;
ALTER TABLE api_test_histories ADD CONSTRAINT fk_api_test_histories_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;
ALTER TABLE api_test_histories ADD CONSTRAINT fk_api_test_histories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Alter ai_conversations table
ALTER TABLE ai_conversations MODIFY COLUMN project_id CHAR(36) NOT NULL;
ALTER TABLE ai_conversations MODIFY COLUMN title VARCHAR(200) NOT NULL;
ALTER TABLE ai_conversations MODIFY COLUMN mode VARCHAR(50) NOT NULL DEFAULT 'chat';
ALTER TABLE ai_conversations MODIFY COLUMN is_pinned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ai_conversations MODIFY COLUMN is_archived BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ai_conversations MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE ai_conversations MODIFY COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE ai_conversations DROP FOREIGN KEY fk_ai_conversations_project;
ALTER TABLE ai_conversations DROP FOREIGN KEY fk_ai_conversations_user;
ALTER TABLE ai_conversations ADD CONSTRAINT fk_ai_conversations_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;
ALTER TABLE ai_conversations ADD CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Alter payments table
ALTER TABLE payments MODIFY COLUMN user_id CHAR(36) NOT NULL;
ALTER TABLE payments MODIFY COLUMN order_code VARCHAR(100) NOT NULL;
ALTER TABLE payments MODIFY COLUMN provider VARCHAR(50) DEFAULT 'BANK_TRANSFER';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS bank_name VARCHAR(100) AFTER provider;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS account_name VARCHAR(150) AFTER bank_name;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS account_number VARCHAR(100) AFTER account_name;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS transfer_content TEXT AFTER account_number;
ALTER TABLE payments MODIFY COLUMN plan_id CHAR(36);
ALTER TABLE payments MODIFY COLUMN plan_name VARCHAR(100);
ALTER TABLE payments MODIFY COLUMN cycle VARCHAR(30);
ALTER TABLE payments MODIFY COLUMN amount DECIMAL(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE payments MODIFY COLUMN currency VARCHAR(10) NOT NULL DEFAULT 'VND';
ALTER TABLE payments MODIFY COLUMN status ENUM('PENDING', 'PAID', 'SUCCESS', 'FAILED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING';
ALTER TABLE payments MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE payments MODIFY COLUMN expired_at TIMESTAMP NULL;
ALTER TABLE payments MODIFY COLUMN paid_at TIMESTAMP NULL;
ALTER TABLE payments MODIFY COLUMN cancelled_at TIMESTAMP NULL;
ALTER TABLE payments MODIFY COLUMN failed_at TIMESTAMP NULL;
ALTER TABLE payments DROP FOREIGN KEY fk_payments_user;
ALTER TABLE payments DROP FOREIGN KEY fk_payments_subscription;
ALTER TABLE payments DROP FOREIGN KEY fk_payments_plan;
ALTER TABLE payments ADD CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE payments ADD CONSTRAINT fk_payments_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL;
ALTER TABLE payments ADD CONSTRAINT fk_payments_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE SET NULL;
-- Add unique constraint for order_code if not already unique
ALTER TABLE payments DROP INDEX uk_payments_order_code;
ALTER TABLE payments ADD CONSTRAINT uk_payments_order_code UNIQUE (order_code);

-- Create payment_events table if not exists (needs to be created after payments table is updated)
CREATE TABLE IF NOT EXISTS payment_events (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    payment_id CHAR(36) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_events_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- Alter activity_logs table
ALTER TABLE activity_logs MODIFY COLUMN module VARCHAR(100) NOT NULL;
ALTER TABLE activity_logs MODIFY COLUMN category VARCHAR(100);
ALTER TABLE activity_logs MODIFY COLUMN action VARCHAR(100) NOT NULL;
ALTER TABLE activity_logs MODIFY COLUMN description TEXT;
ALTER TABLE activity_logs MODIFY COLUMN status VARCHAR(50) DEFAULT 'SUCCESS';
ALTER TABLE activity_logs MODIFY COLUMN metadata_json JSON;
ALTER TABLE activity_logs MODIFY COLUMN is_hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE activity_logs MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE activity_logs DROP FOREIGN KEY fk_activity_logs_user;
ALTER TABLE activity_logs DROP FOREIGN KEY fk_activity_logs_project;
ALTER TABLE activity_logs ADD CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE activity_logs ADD CONSTRAINT fk_activity_logs_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

-- Alter admin_audit_logs table
ALTER TABLE admin_audit_logs MODIFY COLUMN action VARCHAR(100) NOT NULL;
ALTER TABLE admin_audit_logs MODIFY COLUMN description TEXT;
ALTER TABLE admin_audit_logs MODIFY COLUMN metadata_json JSON;
ALTER TABLE admin_audit_logs MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE admin_audit_logs DROP FOREIGN KEY fk_admin_audit_logs_admin;
ALTER TABLE admin_audit_logs DROP FOREIGN KEY fk_admin_audit_logs_target_user;
ALTER TABLE admin_audit_logs ADD CONSTRAINT fk_admin_audit_logs_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE admin_audit_logs ADD CONSTRAINT fk_admin_audit_logs_target_user FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Alter api_documentations table
ALTER TABLE api_documentations ADD COLUMN IF NOT EXISTS description TEXT NULL AFTER title;
ALTER TABLE api_documentations MODIFY COLUMN title VARCHAR(200) NOT NULL;
ALTER TABLE api_documentations ALTER COLUMN version SET DEFAULT '1.0.0';

-- Alter api_documentation_endpoints table
ALTER TABLE api_documentation_endpoints ADD COLUMN IF NOT EXISTS ordinal_position INT NOT NULL DEFAULT 0 AFTER error_example;
ALTER TABLE api_documentation_endpoints MODIFY COLUMN method ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE') NOT NULL;
ALTER TABLE api_documentation_endpoints MODIFY COLUMN headers_json JSON NULL;
ALTER TABLE api_documentation_endpoints MODIFY COLUMN params_json JSON NULL;
