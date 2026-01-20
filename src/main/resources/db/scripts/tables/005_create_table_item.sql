CREATE TABLE IF NOT EXISTS item (
    item_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(150) NOT NULL,
    quantity NUMERIC(10, 3) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    avg_consumption_value NUMERIC(10, 3) NOT NULL,
    avg_consumption_unit VARCHAR(6) NOT NULL,
    avg_consumption_per_day NUMERIC(10, 3) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    in_shopping_list BOOLEAN DEFAULT FALSE,
    last_stock_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    critical_quantity_days_override INT DEFAULT 5,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES category(category_id)
);