-- V1: Users schema (users table + all user-related sub-tables)

CREATE TABLE IF NOT EXISTS users (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255),
    full_name   VARCHAR(255),
    avatar_url  VARCHAR(512),
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER',
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL
);

CREATE TABLE IF NOT EXISTS api_key (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS otp_verification (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_token (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS user_session (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE TABLE IF NOT EXISTS user_setting (
    id          CHAR(36)  NOT NULL PRIMARY KEY,
    created_at  DATETIME  NOT NULL,
    updated_at  DATETIME  NOT NULL
);

CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_username ON users(username);
