-- V3: Database Designer schema

CREATE TABLE IF NOT EXISTS database_schema (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS database_table (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS database_column (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS database_relationship (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS database_index (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);
