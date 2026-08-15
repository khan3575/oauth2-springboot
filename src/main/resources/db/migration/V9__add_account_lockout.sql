-- Split into separate statements: H2 (used for local dev/test) doesn't support
-- multiple comma-separated ADD COLUMN clauses in a single ALTER TABLE.
ALTER TABLE app_user ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE app_user ADD COLUMN locked_until TIMESTAMP WITH TIME ZONE;
