DROP TABLE IF EXISTS event CASCADE;
DROP TYPE IF EXISTS event_type CASCADE;

CREATE TYPE event_type AS ENUM (
    'criado',
    'saque',
    'deposito',
    'transferencia_origem',
    'transferencia_destino',
    'gerente_alterado'
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