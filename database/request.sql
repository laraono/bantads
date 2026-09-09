DROP TABLE IF EXISTS account_history CASCADE;
DROP TABLE IF EXISTS account_data CASCADE;

DROP TYPE IF EXISTS transaction_type CASCADE;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE transaction_type AS ENUM (
    'deposito',
    'saque',
    'transferencia'
);

CREATE TABLE account_data (
    client_cpf VARCHAR(11) NOT NULL,
    account_number VARCHAR(4) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    manager_cpf VARCHAR(11) NOT NULL,
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE account_history (
    history_id SERIAL PRIMARY KEY,
    account_number VARCHAR(4) NOT NULL,
    type transaction_type NOT NULL,
    origin_client_cpf VARCHAR(11) NOT NULL,
    origin_client_name VARCHAR(50) NULL,
    destination_client_cpf VARCHAR(11),
    destination_client_name VARCHAR(40),
    amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE request(
    request_id SERIAL PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    version BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW() 
);

