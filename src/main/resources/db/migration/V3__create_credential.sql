CREATE TABLE credential (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL
        CHECK (type IN ('password', 'webauthn')),
    secret_hash VARCHAR(255) NULL, -- Argon2id hash; null for non-password types
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP WITH TIME ZONE NULL
);

CREATE INDEX idx_credential_user_id ON credential(user_id);