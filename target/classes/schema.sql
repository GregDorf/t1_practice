CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    client_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid()
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    account VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    balance DOUBLE PRECISION NOT NULL,
    account_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    frozen_amount NUMERIC(19,4) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    status VARCHAR(255) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    transaction_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid()
);

CREATE TABLE IF NOT EXISTS data_source_error_log (
    id BIGSERIAL PRIMARY KEY,
    stacktrace TEXT,
    message TEXT NOT NULL,
    method_signature TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS time_limit_exceed_log (
    id BIGSERIAL PRIMARY KEY,
    class_name VARCHAR(255),
    method_name VARCHAR(255),
    execution_time BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);
