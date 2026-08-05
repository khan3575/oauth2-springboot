CREATE TABLE session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE NULL,
    user_agent VARCHAR(512) NULL,
    ip VARCHAR(45) NULL, -- supports IPv4 and IPv6
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_session_user ON session(user_id);