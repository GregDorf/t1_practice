CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    client_id UUID
);

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id) ON DELETE CASCADE,
    account VARCHAR(255),
    balance DOUBLE PRECISION
);

CREATE TABLE transactions (
     id BIGSERIAL PRIMARY KEY,
     account_id BIGINT REFERENCES accounts(id) ON DELETE CASCADE,
     amount NUMERIC,
     created_at TIMESTAMP
);
