CREATE TABLE email_verification_token (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_verification_token_user ON email_verification_token(user_id);