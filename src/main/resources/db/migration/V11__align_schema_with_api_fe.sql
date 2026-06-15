-- 15. workspaces
CREATE TABLE IF NOT EXISTS workspaces (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL UNIQUE,
    name VARCHAR(150),
    config_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workspaces_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 16. ai_conversations
CREATE TABLE IF NOT EXISTS ai_conversations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    user_id CHAR(36),
    title VARCHAR(200) NOT NULL,
    mode VARCHAR(50) NOT NULL DEFAULT 'chat',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_conversations_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_ai_conversations_project_id (project_id)
);

-- 17. ai_messages
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

-- 18. database_schemas
CREATE TABLE IF NOT EXISTS database_schemas (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    db_type VARCHAR(50) NOT NULL DEFAULT 'mysql',
    name VARCHAR(150) DEFAULT 'default_schema',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_database_schemas_project_name (project_id, name),
    CONSTRAINT fk_database_schemas_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_database_schemas_project_id (project_id)
);

-- 19. database_tables
CREATE TABLE IF NOT EXISTS database_tables (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    schema_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    display_name VARCHAR(150),
    row_count INT NOT NULL DEFAULT 0,
    position_x INT DEFAULT 0,
    position_y INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_database_tables_schema_name (schema_id, name),
    CONSTRAINT fk_database_tables_schema FOREIGN KEY (schema_id) REFERENCES database_schemas(id) ON DELETE CASCADE,
    INDEX idx_database_tables_schema_id (schema_id)
);

-- 20. database_columns
CREATE TABLE IF NOT EXISTS database_columns (
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
    UNIQUE KEY uk_database_columns_table_name (table_id, name),
    CONSTRAINT fk_database_columns_table FOREIGN KEY (table_id) REFERENCES database_tables(id) ON DELETE CASCADE,
    INDEX idx_database_columns_table_id (table_id)
);

-- 21. database_relationships
CREATE TABLE IF NOT EXISTS database_relationships (
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
    CONSTRAINT fk_relationships_schema FOREIGN KEY (schema_id) REFERENCES database_schemas(id) ON DELETE CASCADE,
    CONSTRAINT fk_relationships_source_table FOREIGN KEY (source_table_id) REFERENCES database_tables(id) ON DELETE CASCADE,
    CONSTRAINT fk_relationships_source_column FOREIGN KEY (source_column_id) REFERENCES database_columns(id) ON DELETE CASCADE,
    CONSTRAINT fk_relationships_target_table FOREIGN KEY (target_table_id) REFERENCES database_tables(id) ON DELETE CASCADE,
    CONSTRAINT fk_relationships_target_column FOREIGN KEY (target_column_id) REFERENCES database_columns(id) ON DELETE CASCADE
);

-- 22. database_indexes
CREATE TABLE IF NOT EXISTS database_indexes (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    table_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    columns_json JSON NOT NULL,
    is_unique BOOLEAN NOT NULL DEFAULT FALSE,
    index_type VARCHAR(50) DEFAULT 'BTREE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_database_indexes_table_name (table_id, name),
    CONSTRAINT fk_database_indexes_table FOREIGN KEY (table_id) REFERENCES database_tables(id) ON DELETE CASCADE
);

-- 23. api_collections
CREATE TABLE IF NOT EXISTS api_collections (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_collections_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_api_collections_project_id (project_id)
);

-- 24. api_folders
CREATE TABLE IF NOT EXISTS api_folders (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    collection_id CHAR(36) NOT NULL,
    parent_folder_id CHAR(36),
    name VARCHAR(150) NOT NULL,
    ordinal_position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_folders_collection FOREIGN KEY (collection_id) REFERENCES api_collections(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_folders_parent FOREIGN KEY (parent_folder_id) REFERENCES api_folders(id) ON DELETE CASCADE
);

-- 25. api_requests
CREATE TABLE IF NOT EXISTS api_requests (
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
    CONSTRAINT fk_api_requests_collection FOREIGN KEY (collection_id) REFERENCES api_collections(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_requests_folder FOREIGN KEY (folder_id) REFERENCES api_folders(id) ON DELETE SET NULL,
    INDEX idx_api_requests_collection_id (collection_id)
);

-- 26. environments
CREATE TABLE IF NOT EXISTS environments (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_environments_project_name (project_id, name),
    CONSTRAINT fk_environments_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_environments_project_id (project_id)
);

-- 27. environment_variables
CREATE TABLE IF NOT EXISTS environment_variables (
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
    UNIQUE KEY uk_environment_variables_env_key (environment_id, variable_key),
    CONSTRAINT fk_environment_variables_environment FOREIGN KEY (environment_id) REFERENCES environments(id) ON DELETE CASCADE,
    INDEX idx_environment_variables_environment_id (environment_id)
);

-- 28. active_environments
CREATE TABLE IF NOT EXISTS active_environments (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL UNIQUE,
    environment_id CHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_active_environments_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_active_environments_environment FOREIGN KEY (environment_id) REFERENCES environments(id) ON DELETE SET NULL
);

-- 29. api_test_histories
CREATE TABLE IF NOT EXISTS api_test_histories (
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
    CONSTRAINT fk_api_test_histories_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_test_histories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_api_test_histories_project_id (project_id),
    INDEX idx_api_test_histories_user_id (user_id),
    INDEX idx_api_test_histories_created_at (created_at)
);

-- 30. mock_endpoints
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
