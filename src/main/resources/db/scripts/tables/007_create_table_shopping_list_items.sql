CREATE TABLE IF NOT EXISTS shopping_list_item (
    shopping_list_item_id BIGSERIAL PRIMARY KEY,
    shopping_list_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    purchased_quantity NUMERIC(10, 3) NOT NULL,
    unitary_price NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shopping_list FOREIGN KEY (shopping_list_id) REFERENCES shopping_list(shopping_list_id),
    CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES item(item_id)
);