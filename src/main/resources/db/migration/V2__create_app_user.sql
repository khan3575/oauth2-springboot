CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE, -- app normalizes to lowercase before insert/lookup, so plain UNIQUE is enough
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL DEFAULT 'pending_verification'
        CHECK (status IN ('pending_verification', 'active', 'suspended', 'deleted')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_app_user_status ON app_user(status);