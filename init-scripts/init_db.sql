CREATE DATABASE client_db;
CREATE DATABASE manager_db;
CREATE DATABASE auth_db;
CREATE DATABASE request_db;
CREATE DATABASE command_db;


\c command_db;

CREATE TYPE event_type AS ENUM ('criado', 'saque', 'deposito', 'transferencia_origem', 'transferencia_destino', 'gerente_alterado');

CREATE TABLE event (
    event_id       		UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_id      		VARCHAR(4) NOT NULL,
    type				event_type NOT NULL,
    payload           	JSON NOT NULL,
    version        		BIGINT NOT NULL,
    created_at     		TIMESTAMP NOT NULL DEFAULT now(),

    UNIQUE(object_id, version)
);


\c request_db;

CREATE TYPE transaction_type AS ENUM ('deposito', 'saque', 'transferencia');

CREATE TABLE account_data (
	client_cpf			VARCHAR(11) NOT NULL,
	account_number		VARCHAR(4) PRIMARY KEY NOT NULL,
	created_at			TIMESTAMP NOT NULL,
	manager_cpf			VARCHAR(11) NOT NULL,
	balance				NUMERIC(19, 4) NOT NULL,
	deleted_at 			TIMESTAMP NULL
);

CREATE TABLE account_history (
	history_id			        SERIAL PRIMARY KEY,
	account_number			    VARCHAR(4) NOT NULL,
	type				        transaction_type NOT NULL,
	origin_client_cpf		    VARCHAR(11) NOT NULL,
	origin_client_name		    VARCHAR(50) NOT NULL,
	destination_client_cpf		VARCHAR(11) NULL,
	destination_client_name		VARCHAR(40) NULL,
	amount				        NUMERIC(19, 4) NOT NULL,
	created_at			        TIMESTAMP NOT NULL DEFAULT NOW()
);


\c manager_db;

CREATE TABLE manager (
    manager_id      SERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    cpf             VARCHAR(11) NOT NULL UNIQUE,
    phone           VARCHAR(20) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    is_active       BOOLEAN NOT NULL
);


\c client_db;

CREATE TABLE state(
    state_id        SERIAL PRIMARY KEY,
    uf              VARCHAR(2) NOT NULL UNIQUE
);

INSERT INTO state (uf) VALUES
    ('AC'),('AL'),('AP'),('AM'),('BA'),('CE'),('DF'),('ES'),('GO'),('MA'),
    ('MT'),('MS'),('MG'),('PA'),('PB'),('PE'),('PI'),('PR'),('RJ'),('RN'),
    ('RS'),('RO'),('RR'),('SC'),('SP'),('SE'),('TO');

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

CREATE TYPE approved_status AS ENUM('PENDENTE', 'APROVADO', 'NAO_APROVADO');

CREATE TABLE request (
    request_id          SERIAL PRIMARY KEY,
    client_id           INTEGER NOT NULL,
    status              approved_status NOT NULL,
    rejection_reason    VARCHAR(255) NULL,
    approved_at         TIMESTAMP NULL
);

