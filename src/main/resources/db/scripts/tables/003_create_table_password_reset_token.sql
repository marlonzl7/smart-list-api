CREATE TABLE IF NOT EXISTS password_reset_token (
    password_reset_token_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email VARCHAR(100) NOT NULL,
    token UUID NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    request_ip VARCHAR(45) NOT NULL,
    user_agent TEXT NOT NULL,

    CONSTRAINT chkStatus CHECK (status IN ('PENDING', 'USED', 'EXPIRED'))
);