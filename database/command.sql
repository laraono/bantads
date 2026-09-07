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
    object_id VARCHAR(4) NOT NULL,
    type event_type NOT NULL,
    payload JSON NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_event_object_version
        UNIQUE (object_id, version)
);

