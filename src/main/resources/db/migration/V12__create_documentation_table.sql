CREATE TABLE IF NOT EXISTS api_documentations (
    id CHAR(36) NOT NULL,
    project_id CHAR(36) NOT NULL,
    title VARCHAR(255),
    version VARCHAR(50),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_documentations_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uk_documentations_project UNIQUE (project_id)
);

CREATE TABLE IF NOT EXISTS api_documentation_endpoints (
    id CHAR(36) NOT NULL,
    documentation_id CHAR(36) NOT NULL,
    method VARCHAR(20) NOT NULL,
    url TEXT NOT NULL,
    description TEXT,
    headers_json LONGTEXT,
    params_json LONGTEXT,
    body_example LONGTEXT,
    response_example LONGTEXT,
    error_example LONGTEXT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_endpoints_documentation FOREIGN KEY (documentation_id) REFERENCES api_documentations(id) ON DELETE CASCADE
);

CREATE INDEX idx_api_documentations_project_id ON api_documentations(project_id);
CREATE INDEX idx_api_documentation_endpoints_doc_id ON api_documentation_endpoints(documentation_id);
