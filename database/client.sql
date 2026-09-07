DROP TABLE IF EXISTS request CASCADE;
DROP TABLE IF EXISTS client CASCADE;
DROP TABLE IF EXISTS address CASCADE;
DROP TABLE IF EXISTS state CASCADE;
DROP TYPE IF EXISTS approved_status CASCADE;

CREATE TYPE approved_status AS ENUM (
    'PENDENTE',
    'APROVADO',
    'NAO_APROVADO'
)

CREATE TABLE state(
    state_id        SERIAL PRIMARY KEY,
    uf              VARCHAR(2) NOT NULL
);

CREATE TABLE address(
    address_id      SERIAL PRIMARY KEY,
    cep                 VARCHAR(9) NOT NULL,
    city                VARCHAR(30) NOT NULL,
    street              VARCHAR(30) NOT NULL,
    number              INTEGER NOT NULL,
    additional_info     VARCHAR(30) NULL,
    state_id            INTEGER NOT NULL,

    FOREIGN KEY(state_id) REFERENCES state(state_id)
);

CREATE TABLE state(
    state_id        SERIAL PRIMARY KEY,
    uf              VARCHAR(2) NOT NULL
);

CREATE TABLE address(
    address_id      SERIAL PRIMARY KEY,
    cep                 VARCHAR(9) NOT NULL,
    city                VARCHAR(30) NOT NULL,
    street              VARCHAR(30) NOT NULL,
    number              INTEGER NOT NULL,
    additional_info     VARCHAR(30) NULL,
    state_id            INTEGER NOT NULL,

    FOREIGN KEY(state_id) REFERENCES state(state_id)
);

CREATE TABLE client (
    client_id           SERIAL PRIMARY KEY,
    name                VARCHAR(50) NOT NULL,
    email               VARCHAR(100) NOT NULL, 
    cpf                 VARCHAR(11) NOT NULL UNIQUE,
    phone               VARCHAR(20) NOT NULL,
    salary              NUMERIC(19, 4) NOT NULL,
    address_id          INTEGER NOT NULL,

    FOREIGN KEY(address_id) REFERENCES address(address_id)
);

CREATE TABLE request (
    request_id          SERIAL PRIMARY KEY,
    client_id           INTEGER NOT NULL,
    status              request_status NOT NULL,
    rejection_reason    VARCHAR(255) NULL,
    approved_at         TIMESTAMP NULL,

    FOREIGN KEY(client_id) REFERENCES client(client_id)
);
