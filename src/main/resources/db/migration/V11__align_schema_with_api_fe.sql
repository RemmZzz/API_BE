-- Align users table with constraints and types
ALTER TABLE users MODIFY COLUMN username VARCHAR(50) NOT NULL;
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER';
ALTER TABLE users MODIFY COLUMN status ENUM('ACTIVE', 'INACTIVE', 'BANNED', 'PENDING') NOT NULL DEFAULT 'PENDING';

-- Align user_settings table
ALTER TABLE user_settings ADD COLUMN user_id CHAR(36) NOT NULL UNIQUE;
ALTER TABLE user_settings ADD COLUMN language VARCHAR(10) NOT NULL DEFAULT 'vi';
ALTER TABLE user_settings ADD COLUMN notification_settings JSON NULL;
ALTER TABLE user_settings ADD COLUMN privacy_settings JSON NULL;
ALTER TABLE user_settings ADD CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Align projects table
ALTER TABLE projects MODIFY COLUMN owner_id CHAR(36) NOT NULL;
ALTER TABLE projects MODIFY COLUMN name VARCHAR(150) NOT NULL;
ALTER TABLE projects ADD COLUMN description TEXT NULL;
ALTER TABLE projects ADD COLUMN type VARCHAR(50) NULL;
ALTER TABLE projects ADD COLUMN color VARCHAR(30) NULL;
ALTER TABLE projects MODIFY COLUMN status ENUM('ACTIVE', 'ARCHIVED', 'DELETED') NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE projects ADD CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE;

-- Align project_members table
ALTER TABLE project_members ADD COLUMN project_id CHAR(36) NOT NULL;
ALTER TABLE project_members ADD COLUMN user_id CHAR(36) NOT NULL;
ALTER TABLE project_members ADD COLUMN role ENUM('OWNER', 'ADMIN', 'MEMBER', 'VIEWER') NOT NULL DEFAULT 'MEMBER';
ALTER TABLE project_members ADD CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;
ALTER TABLE project_members ADD CONSTRAINT fk_project_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE project_members ADD CONSTRAINT uk_project_members_project_user UNIQUE (project_id, user_id);

-- Align database_schemas table
ALTER TABLE database_schemas ADD COLUMN project_id CHAR(36) NOT NULL;
ALTER TABLE database_schemas ADD COLUMN db_type VARCHAR(50) NOT NULL DEFAULT 'mysql';
ALTER TABLE database_schemas ADD COLUMN name VARCHAR(150) DEFAULT 'default_schema';
ALTER TABLE database_schemas ADD CONSTRAINT fk_database_schemas_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;
ALTER TABLE database_schemas ADD CONSTRAINT uk_database_schemas_project_name UNIQUE (project_id, name);

-- Align database_tables table
ALTER TABLE database_tables ADD COLUMN schema_id CHAR(36) NOT NULL;
ALTER TABLE database_tables ADD COLUMN name VARCHAR(150) NOT NULL;
ALTER TABLE database_tables ADD COLUMN display_name VARCHAR(150) NULL;
ALTER TABLE database_tables ADD COLUMN row_count INT NOT NULL DEFAULT 0;
ALTER TABLE database_tables ADD COLUMN position_x INT DEFAULT 0;
ALTER TABLE database_tables ADD COLUMN position_y INT DEFAULT 0;
ALTER TABLE database_tables ADD CONSTRAINT fk_database_tables_schema FOREIGN KEY (schema_id) REFERENCES database_schemas(id) ON DELETE CASCADE;
ALTER TABLE database_tables ADD CONSTRAINT uk_database_tables_schema_name UNIQUE (schema_id, name);

-- Align database_columns table
ALTER TABLE database_columns ADD COLUMN table_id CHAR(36) NOT NULL;
ALTER TABLE database_columns ADD COLUMN name VARCHAR(150) NOT NULL;
ALTER TABLE database_columns ADD COLUMN data_type VARCHAR(100) NOT NULL;
ALTER TABLE database_columns ADD COLUMN length INT NULL;
ALTER TABLE database_columns ADD COLUMN precision_value INT NULL;
ALTER TABLE database_columns ADD COLUMN scale_value INT NULL;
ALTER TABLE database_columns ADD COLUMN is_primary_key BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE database_columns ADD COLUMN is_nullable BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE database_columns ADD COLUMN is_unique BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE database_columns ADD COLUMN is_auto_increment BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE database_columns ADD COLUMN default_value TEXT NULL;
ALTER TABLE database_columns ADD COLUMN ordinal_position INT NOT NULL DEFAULT 0;
ALTER TABLE database_columns ADD COLUMN comment TEXT NULL;
ALTER TABLE database_columns ADD CONSTRAINT fk_database_columns_table FOREIGN KEY (table_id) REFERENCES database_tables(id) ON DELETE CASCADE;
ALTER TABLE database_columns ADD CONSTRAINT uk_database_columns_table_name UNIQUE (table_id, name);

-- Align database_relationships table
ALTER TABLE database_relationships ADD COLUMN schema_id CHAR(36) NOT NULL;
ALTER TABLE database_relationships ADD COLUMN source_table_id CHAR(36) NOT NULL;
ALTER TABLE database_relationships ADD COLUMN source_column_id CHAR(36) NOT NULL;
ALTER TABLE database_relationships ADD COLUMN target_table_id CHAR(36) NOT NULL;
ALTER TABLE database_relationships ADD COLUMN target_column_id CHAR(36) NOT NULL;
ALTER TABLE database_relationships ADD COLUMN constraint_name VARCHAR(150) NULL;
ALTER TABLE database_relationships ADD COLUMN on_delete_action VARCHAR(50) DEFAULT 'NO ACTION';
ALTER TABLE database_relationships ADD COLUMN on_update_action VARCHAR(50) DEFAULT 'NO ACTION';
ALTER TABLE database_relationships ADD CONSTRAINT fk_relationships_schema FOREIGN KEY (schema_id) REFERENCES database_schemas(id) ON DELETE CASCADE;
ALTER TABLE database_relationships ADD CONSTRAINT fk_relationships_source_table FOREIGN KEY (source_table_id) REFERENCES database_tables(id) ON DELETE CASCADE;
ALTER TABLE database_relationships ADD CONSTRAINT fk_relationships_source_column FOREIGN KEY (source_column_id) REFERENCES database_columns(id) ON DELETE CASCADE;
ALTER TABLE database_relationships ADD CONSTRAINT fk_relationships_target_table FOREIGN KEY (target_table_id) REFERENCES database_tables(id) ON DELETE CASCADE;
ALTER TABLE database_relationships ADD CONSTRAINT fk_relationships_target_column FOREIGN KEY (target_column_id) REFERENCES database_columns(id) ON DELETE CASCADE;

-- Align database_indexes table
ALTER TABLE database_indexes ADD COLUMN table_id CHAR(36) NOT NULL;
ALTER TABLE database_indexes ADD COLUMN name VARCHAR(150) NOT NULL;
ALTER TABLE database_indexes ADD COLUMN columns_json JSON NOT NULL;
ALTER TABLE database_indexes ADD COLUMN is_unique BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE database_indexes ADD COLUMN index_type VARCHAR(50) DEFAULT 'BTREE';
ALTER TABLE database_indexes ADD CONSTRAINT fk_database_indexes_table FOREIGN KEY (table_id) REFERENCES database_tables(id) ON DELETE CASCADE;
ALTER TABLE database_indexes ADD CONSTRAINT uk_database_indexes_table_name UNIQUE (table_id, name);

-- Create workspaces table
CREATE TABLE workspaces (
    id CHAR(36) PRIMARY KEY,
    project_id CHAR(36) NOT NULL UNIQUE,
    name VARCHAR(150) NULL,
    config_json JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workspaces_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Create environments table
CREATE TABLE environments (
    id CHAR(36) PRIMARY KEY,
    project_id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_environments_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uk_environments_project_name UNIQUE (project_id, name)
);

-- Create environment_variables table
CREATE TABLE environment_variables (
    id CHAR(36) PRIMARY KEY,
    environment_id CHAR(36) NOT NULL,
    variable_key VARCHAR(100) NOT NULL,
    initial_value TEXT NULL,
    current_value TEXT NULL,
    type VARCHAR(50) DEFAULT 'default',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_secret BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_environment_variables_environment FOREIGN KEY (environment_id) REFERENCES environments(id) ON DELETE CASCADE,
    CONSTRAINT uk_environment_variables_env_key UNIQUE (environment_id, variable_key)
);

-- Create active_environments table
CREATE TABLE active_environments (
    id CHAR(36) PRIMARY KEY,
    project_id CHAR(36) NOT NULL UNIQUE,
    environment_id CHAR(36) NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_active_environments_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_active_environments_environment FOREIGN KEY (environment_id) REFERENCES environments(id) ON DELETE SET NULL
);

-- Create api_collections table
CREATE TABLE api_collections (
    id CHAR(36) PRIMARY KEY,
    project_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_collections_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- Create api_folders table
CREATE TABLE api_folders (
    id CHAR(36) PRIMARY KEY,
    collection_id CHAR(36) NOT NULL,
    parent_folder_id CHAR(36) NULL,
    name VARCHAR(150) NOT NULL,
    ordinal_position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_folders_collection FOREIGN KEY (collection_id) REFERENCES api_collections(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_folders_parent FOREIGN KEY (parent_folder_id) REFERENCES api_folders(id) ON DELETE CASCADE
);

-- Create api_requests table
CREATE TABLE api_requests (
    id CHAR(36) PRIMARY KEY,
    collection_id CHAR(36) NOT NULL,
    folder_id CHAR(36) NULL,
    name VARCHAR(150) NOT NULL,
    method ENUM('GET', 'POST', 'PUT', 'PATCH', 'DELETE') NOT NULL DEFAULT 'GET',
    url TEXT NOT NULL,
    description TEXT NULL,
    headers_json JSON NULL,
    params_json JSON NULL,
    body LONGTEXT NULL,
    body_type VARCHAR(50) DEFAULT 'json',
    response_example LONGTEXT NULL,
    ordinal_position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_requests_collection FOREIGN KEY (collection_id) REFERENCES api_collections(id) ON DELETE CASCADE,
    CONSTRAINT fk_api_requests_folder FOREIGN KEY (folder_id) REFERENCES api_folders(id) ON DELETE SET NULL
);

-- Indexes
CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);
CREATE INDEX idx_database_schemas_project_id ON database_schemas(project_id);
CREATE INDEX idx_database_tables_schema_id ON database_tables(schema_id);
CREATE INDEX idx_database_columns_table_id ON database_columns(table_id);
CREATE INDEX idx_api_collections_project_id ON api_collections(project_id);
CREATE INDEX idx_api_requests_collection_id ON api_requests(collection_id);
CREATE INDEX idx_environments_project_id ON environments(project_id);
CREATE INDEX idx_environment_variables_environment_id ON environment_variables(environment_id);
