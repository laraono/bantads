DROP TABLE IF EXISTS account_history CASCADE;
DROP TABLE IF EXISTS account_data CASCADE;
DROP TABLE IF EXISTS event CASCADE;

DROP TYPE IF EXISTS transaction_type CASCADE;
DROP TYPE IF EXISTS event_type CASCADE;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE event_type AS ENUM (
    'criado',
    'saque',
    'deposito',
    'transferencia_origem',
    'transferencia_destino',
    'gerente_alterado'
);

CREATE TYPE transaction_type AS ENUM (
    'deposito',
    'saque',
    'transferencia'
);

CREATE TABLE event (
    event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_id BIGINT NOT NULL,
    type event_type NOT NULL,
    payload JSON NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_event_object_version
        UNIQUE (object_id, version)
);

CREATE TABLE account_data (
    client_cpf VARCHAR(11) NOT NULL,
    account_number BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    manager_cpf VARCHAR(11) NOT NULL,
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE account_history (
    history_id SERIAL PRIMARY KEY,
    account_number BIGINT NOT NULL,
    type transaction_type NOT NULL,
    origin_client_cpf VARCHAR(11) NOT NULL,
    origin_client_name VARCHAR(50) NOT NULL,
    destination_client_cpf VARCHAR(11),
    destination_client_name VARCHAR(40),
    amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO event (
    object_id,
    version,
    type,
    payload,
    created_at
)
VALUES
(
    1291,
    1,
    'criado',
    '{"name":"Catharyna","cpf":"12912861012","manager_cpf":"98574307084"}',
    '2000-01-01 00:00:00'
),
(
    1291,
    2,
    'deposito',
    '{"name":"Catharyna","cpf":"12912861012","amount":"1000.00"}',
    '2020-01-01 10:00:00'
),
(
    1291,
    3,
    'deposito',
    '{"name":"Catharyna","cpf":"12912861012","amount":"900.00"}',
    '2020-01-01 11:00:00'
),
(
    1291,
    4,
    'saque',
    '{"name":"Catharyna","cpf":"12912861012","amount":"550.00"}',
    '2020-01-01 12:00:00'
),
(
    1291,
    5,
    'saque',
    '{"name":"Catharyna","cpf":"12912861012","amount":"350.00"}',
    '2020-01-01 13:00:00'
),
(
    1291,
    6,
    'deposito',
    '{"name":"Catharyna","cpf":"12912861012","amount":"2000.00"}',
    '2020-01-10 15:00:00'
),
(
    1291,
    7,
    'saque',
    '{"name":"Catharyna","cpf":"12912861012","amount":"500.00"}',
    '2020-01-15 08:00:00'
),
(
    1291,
    8,
    'transferencia_origem',
    '{"name":"Catharyna","cpf":"12912861012","amount":"1700.00","dest_name":"Cleuddônio","dest_cpf":"09506382000"}',
    '2020-01-20 12:00:00'
),
(
    950,
    1,
    'criado',
    '{"name":"Cleuddônio","cpf":"09506382000","manager_cpf":"64065268052"}',
    '1990-10-10 00:00:00'
),
(
    950,
    2,
    'transferencia_destino',
    '{"name":"Cleuddônio","cpf":"09506382000","amount":"1700.00","origin_name":"Catharyna","origin_cpf":"12912861012"}',
    '2020-01-20 12:00:00'
),
(
    950,
    3,
    'deposito',
    '{"name":"Cleuddônio","cpf":"09506382000","amount":"1000.00"}',
    '2025-01-01 12:00:00'
),
(
    950,
    4,
    'deposito',
    '{"name":"Cleuddônio","cpf":"09506382000","amount":"5000.00"}',
    '2025-01-02 10:00:00'
),
(
    950,
    5,
    'saque',
    '{"name":"Cleuddônio","cpf":"09506382000","amount":"200.00"}',
    '2025-01-10 10:00:00'
),
(
    950,
    6,
    'deposito',
    '{"name":"Cleuddônio","cpf":"09506382000","amount":"7000.00"}',
    '2025-02-05 10:00:00'
),
(
    950,
    7,
    'saque',
    '{"name":"Cleuddônio","cpf":"09506382000","amount":"4500.00"}',
    '2025-03-06 11:00:00'
),
(
    8573,
    1,
    'criado',
    '{"name":"Catianna","cpf":"85733854057","manager_cpf":"23862179060"}',
    '2012-12-12 00:00:00'
),
(
    8573,
    2,
    'deposito',
    '{"name":"Catianna","cpf":"85733854057","amount":"1000.00"}',
    '2025-05-05 10:00:00'
),
(
    8573,
    3,
    'saque',
    '{"name":"Catianna","cpf":"85733854057","amount":"800.00"}',
    '2025-05-06 10:00:00'
),
(
    5887,
    1,
    'criado',
    '{"name":"Cutardo","cpf":"58872160006","manager_cpf":"98574307084"}',
    '2022-02-22 00:00:00'
),
(
    5887,
    2,
    'deposito',
    '{"name":"Cutardo","cpf":"58872160006","amount":"150000.00"}',
    '2025-06-01 10:00:00'
),
(
    7617,
    1,
    'criado',
    '{"name":"Coândrya","cpf":"76179646090","manager_cpf":"64065268052"}',
    '2025-01-01 00:00:00'
),
(
    7617,
    2,
    'deposito',
    '{"name":"Coândrya","cpf":"76179646090","amount":"1500.00"}',
    '2025-07-01 10:00:00'
);

INSERT INTO account_data (
    client_cpf,
    account_number,
    created_at,
    manager_cpf,
    balance,
    deleted_at
)
VALUES
(
    '12912861012',
    1291,
    '2000-01-01 00:00:00',
    '98574307084',
    800.0000,
    NULL
),
(
    '09506382000',
    950,
    '1990-10-10 00:00:00',
    '64065268052',
    10000.0000,
    NULL
),
(
    '85733854057',
    8573,
    '2012-12-12 00:00:00',
    '23862179060',
    200.0000,
    NULL
),
(
    '58872160006',
    5887,
    '2022-02-22 00:00:00',
    '98574307084',
    150000.0000,
    NULL
),
(
    '76179646090',
    7617,
    '2025-01-01 00:00:00',
    '64065268052',
    1500.0000,
    NULL
);

INSERT INTO account_history (
    account_number,
    type,
    origin_client_cpf,
    origin_client_name,
    destination_client_cpf,
    destination_client_name,
    amount,
    created_at
)
VALUES
(
    1291,
    'deposito',
    '12912861012',
    'Catharyna',
    NULL,
    NULL,
    1000.0000,
    '2020-01-01 10:00:00'
),
(
    1291,
    'deposito',
    '12912861012',
    'Catharyna',
    NULL,
    NULL,
    900.0000,
    '2020-01-01 11:00:00'
),
(
    1291,
    'saque',
    '12912861012',
    'Catharyna',
    NULL,
    NULL,
    550.0000,
    '2020-01-01 12:00:00'
),
(
    1291,
    'saque',
    '12912861012',
    'Catharyna',
    NULL,
    NULL,
    350.0000,
    '2020-01-01 13:00:00'
),
(
    1291,
    'deposito',
    '12912861012',
    'Catharyna',
    NULL,
    NULL,
    2000.0000,
    '2020-01-10 15:00:00'
),
(
    1291,
    'saque',
    '12912861012',
    'Catharyna',
    NULL,
    NULL,
    500.0000,
    '2020-01-15 08:00:00'
),
(
    1291,
    'transferencia',
    '12912861012',
    'Catharyna',
    '09506382000',
    'Cleuddônio',
    1700.0000,
    '2020-01-20 12:00:00'
),
(
    950,
    'transferencia',
    '12912861012',
    'Catharyna',
    '09506382000',
    'Cleuddônio',
    1700.0000,
    '2020-01-20 12:00:00'
),
(
    950,
    'deposito',
    '09506382000',
    'Cleuddônio',
    NULL,
    NULL,
    1000.0000,
    '2025-01-01 12:00:00'
),
(
    950,
    'deposito',
    '09506382000',
    'Cleuddônio',
    NULL,
    NULL,
    5000.0000,
    '2025-01-02 10:00:00'
),
(
    950,
    'saque',
    '09506382000',
    'Cleuddônio',
    NULL,
    NULL,
    200.0000,
    '2025-01-10 10:00:00'
),
(
    950,
    'deposito',
    '09506382000',
    'Cleuddônio',
    NULL,
    NULL,
    7000.0000,
    '2025-02-05 10:00:00'
),
(
    950,
    'saque',
    '09506382000',
    'Cleuddônio',
    NULL,
    NULL,
    4500.0000,
    '2025-03-06 11:00:00'
),
(
    8573,
    'deposito',
    '85733854057',
    'Catianna',
    NULL,
    NULL,
    1000.0000,
    '2025-05-05 10:00:00'
),
(
    8573,
    'saque',
    '85733854057',
    'Catianna',
    NULL,
    NULL,
    800.0000,
    '2025-05-06 10:00:00'
),
(
    5887,
    'deposito',
    '58872160006',
    'Cutardo',
    NULL,
    NULL,
    150000.0000,
    '2025-06-01 10:00:00'
),
(
    7617,
    'deposito',
    '76179646090',
    'Coândrya',
    NULL,
    NULL,
    1500.0000,
    '2025-07-01 10:00:00'
);
