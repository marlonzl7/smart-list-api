CREATE INDEX idx_password_reset_token_email_status ON password_reset_token (email, status);
