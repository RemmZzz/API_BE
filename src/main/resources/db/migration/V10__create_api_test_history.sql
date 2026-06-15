-- V10: Create api_test_history table for API Tester module

CREATE TABLE IF NOT EXISTS api_test_history (
    id               CHAR(36)     NOT NULL PRIMARY KEY,
    project_id       CHAR(36)     NOT NULL,
    name             VARCHAR(255),
    method           VARCHAR(20)  NOT NULL,
    url              TEXT         NOT NULL,
    request_headers  LONGTEXT,
    request_params   LONGTEXT,
    request_body     LONGTEXT,
    status_code      INT,
    status_text      VARCHAR(100),
    response_headers LONGTEXT,
    response_body    LONGTEXT,
    duration_ms      BIGINT,
    success          TINYINT(1)   NOT NULL DEFAULT 0,
    error_message    TEXT,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    CONSTRAINT fk_api_test_history_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_api_test_history_project_id  ON api_test_history(project_id);
CREATE INDEX idx_api_test_history_method      ON api_test_history(method);
CREATE INDEX idx_api_test_history_created_at  ON api_test_history(created_at);
