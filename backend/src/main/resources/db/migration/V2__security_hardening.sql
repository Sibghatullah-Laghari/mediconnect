-- Security Hardening Migration

-- Make old token column nullable since we are switching to token_hash
ALTER TABLE refresh_tokens ALTER COLUMN token DROP NOT NULL;

-- Add token_hash for refresh tokens (SHA-256 = 64 hex chars)
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS token_hash VARCHAR(64) NOT NULL DEFAULT '';

-- Unique constraint on token_hash for fast lookups and integrity
CREATE UNIQUE INDEX IF NOT EXISTS uk_refresh_tokens_token_hash ON refresh_tokens (token_hash);

-- Account lockout columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_users_locked_until ON users (locked_until);
