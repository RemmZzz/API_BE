-- Complete authentication schema expected by the auth module.

ALTER TABLE users RENAME COLUMN password TO password_hash;
ALTER TABLE users RENAME COLUMN full_name TO name;
ALTER TABLE users ADD COLUMN phone VARCHAR(30) NULL AFTER status;
ALTER TABLE users ADD COLUMN last_login_at DATETIME NULL AFTER avatar_url;

RENAME TABLE otp_verification TO otp_verifications;
ALTER TABLE otp_verifications
    ADD COLUMN user_id CHAR(36) NOT NULL AFTER id,
    ADD COLUMN email VARCHAR(255) NOT NULL AFTER user_id,
    ADD COLUMN otp_hash VARCHAR(255) NOT NULL AFTER email,
    ADD COLUMN purpose VARCHAR(50) NOT NULL AFTER otp_hash,
    ADD COLUMN expires_at DATETIME NOT NULL AFTER purpose,
    ADD COLUMN verified_at DATETIME NULL AFTER expires_at;

RENAME TABLE password_reset_token TO password_reset_tokens;
ALTER TABLE password_reset_tokens
    ADD COLUMN user_id CHAR(36) NOT NULL AFTER id,
    ADD COLUMN token_hash VARCHAR(255) NOT NULL AFTER user_id,
    ADD COLUMN expires_at DATETIME NOT NULL AFTER token_hash,
    ADD COLUMN used_at DATETIME NULL AFTER expires_at;

RENAME TABLE user_session TO user_sessions;
ALTER TABLE user_sessions
    ADD COLUMN user_id CHAR(36) NOT NULL AFTER id,
    ADD COLUMN refresh_token_hash VARCHAR(255) NOT NULL AFTER user_id,
    ADD COLUMN ip_address VARCHAR(100) NULL AFTER refresh_token_hash,
    ADD COLUMN user_agent VARCHAR(512) NULL AFTER ip_address,
    ADD COLUMN expires_at DATETIME NOT NULL AFTER user_agent,
    ADD COLUMN revoked_at DATETIME NULL AFTER expires_at;

CREATE INDEX idx_otp_verifications_email_purpose_created_at ON otp_verifications(email, purpose, created_at);
CREATE INDEX idx_password_reset_tokens_expires_at_used_at ON password_reset_tokens(expires_at, used_at);
CREATE INDEX idx_user_sessions_user_id_expires_at_revoked_at ON user_sessions(user_id, expires_at, revoked_at);
