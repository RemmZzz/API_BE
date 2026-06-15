-- V4: Create collection schema (collections, folders, requests)

CREATE TABLE IF NOT EXISTS collections (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    project_id  CHAR(36)     NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_collections_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS collection_folders (
    id               CHAR(36)     NOT NULL PRIMARY KEY,
    collection_id    CHAR(36)     NOT NULL,
    parent_folder_id CHAR(36),
    name             VARCHAR(255) NOT NULL,
    sort_order       INT          NOT NULL DEFAULT 0,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    CONSTRAINT fk_folders_collection    FOREIGN KEY (collection_id)    REFERENCES collections(id)       ON DELETE CASCADE,
    CONSTRAINT fk_folders_parent_folder FOREIGN KEY (parent_folder_id) REFERENCES collection_folders(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS api_requests (
    id            CHAR(36)      NOT NULL PRIMARY KEY,
    collection_id CHAR(36)      NOT NULL,
    folder_id     CHAR(36),
    name          VARCHAR(255)  NOT NULL,
    method        VARCHAR(20)   NOT NULL DEFAULT 'GET',
    url           TEXT          NOT NULL,
    headers       LONGTEXT,
    params        LONGTEXT,
    body          LONGTEXT,
    description   TEXT,
    sort_order    INT           NOT NULL DEFAULT 0,
    created_at    DATETIME      NOT NULL,
    updated_at    DATETIME      NOT NULL,
    CONSTRAINT fk_requests_collection FOREIGN KEY (collection_id) REFERENCES collections(id)       ON DELETE CASCADE,
    CONSTRAINT fk_requests_folder     FOREIGN KEY (folder_id)     REFERENCES collection_folders(id) ON DELETE SET NULL
);

CREATE INDEX idx_collections_project_id  ON collections(project_id);
CREATE INDEX idx_folders_collection_id   ON collection_folders(collection_id);
CREATE INDEX idx_folders_parent_id       ON collection_folders(parent_folder_id);
CREATE INDEX idx_requests_collection_id  ON api_requests(collection_id);
CREATE INDEX idx_requests_folder_id      ON api_requests(folder_id);
