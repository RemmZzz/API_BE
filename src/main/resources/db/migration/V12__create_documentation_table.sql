-- 31. api_documentations
CREATE TABLE IF NOT EXISTS api_documentations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    project_id CHAR(36) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    version VARCHAR(50) DEFAULT '1.0.0',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_api_documentations_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_api_documentations_project_id (project_id)
);

-- 32. api_documentation_endpoints
CREATE TABLE IF NOT EXISTS api_documentation_endpoints (
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
    CONSTRAINT fk_doc_endpoints_documentation FOREIGN KEY (documentation_id) REFERENCES api_documentations(id) ON DELETE CASCADE,
    INDEX idx_api_documentation_endpoints_doc_id (documentation_id)
);
