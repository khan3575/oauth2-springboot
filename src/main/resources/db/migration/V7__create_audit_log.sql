CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    user_id UUID NULL REFERENCES app_user(id) ON DELETE SET NULL, -- nullable: failed logins with unknown email still get logged
    event_type VARCHAR(100) NOT NULL,
    ip VARCHAR(45) NULL,
    user_agent VARCHAR(512) NULL,
    metadata TEXT NOT NULL DEFAULT '{}', -- JSON string; revisit as real JSONB once on Postgres
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_event_type ON audit_log(event_type);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);