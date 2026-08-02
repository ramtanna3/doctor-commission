-- Add authentication columns to users table
ALTER TABLE users
    ADD COLUMN username VARCHAR(100) NULL AFTER email,
    ADD COLUMN password_hash VARCHAR(255) NULL AFTER username;

-- Unique index on username (allows NULL for backward compat with existing rows)
CREATE UNIQUE INDEX idx_users_username ON users (username);
