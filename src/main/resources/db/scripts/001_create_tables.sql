CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    notification_preference VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    theme_preference VARCHAR(20) NOT NULL DEFAULT 'SYSTEM'
);