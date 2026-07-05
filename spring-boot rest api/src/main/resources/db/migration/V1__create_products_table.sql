CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT products_price_non_negative CHECK (price >= 0),
    CONSTRAINT products_quantity_non_negative CHECK (quantity >= 0)
);
