-- Add failed_attempts column to otp_verifications
ALTER TABLE otp_verifications ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0;

-- Create oauth2_exchange_codes table
CREATE TABLE oauth2_exchange_codes (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_oauth2_exchange_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
