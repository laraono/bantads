CREATE DATABASE client_db;
CREATE DATABASE manager_db;
CREATE DATABASE auth_db;
CREATE DATABASE request_db;
CREATE DATABASE command_db;


\c command_db;

CREATE TYPE event_type AS ENUM ('criado', 'saque', 'deposito', 'transferencia_origem', 'transferencia_destino', 'gerente_alterado');

CREATE TABLE event (
    event_id       		UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_id      		BIGINT NOT NULL,
    type				event_type NOT NULL,
    payload           	JSON NOT NULL,
    version        		BIGINT NOT NULL,
    created_at     		TIMESTAMP NOT NULL DEFAULT now(),
);


\c request_db;

CREATE TYPE transaction_type AS ENUM ('deposito', 'saque', 'transferencia');

CREATE TABLE account_data (
	client_cpf			VARCHAR(11) NOT NULL,
	account_number		VARCHAR(4) PRIMARY KEY NOT NULL,
	created_at			TIMESTAMP NOT NULL,
	manager_cpf			VARCHAR(11) NOT NULL,
	balance				NUMERIC(19, 4),
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
	create_at			        TIMESTAMP NOT NULL DEFAULT NOW()
);


\c manager_db;

CREATE TABLE manager (
    manager_id      SERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    cpf             VARCHAR(11) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    is_active       BOOLEAN NOT NOT NULL
);


\c client_db;

CREATE TABLE client (
    client_id           SERIAL PRIMARY KEY,
    name                VARCHAR(50) NOT NULL,
    email               VARCHAR(100) NOT NULL, 
    cpf                 VARCHAR(11) NOT NULL,
    phone               VARCHAR(20) NOT NULL,
    salary              NUMERIC(19, 4) NOT NULL,
    cep                 VARCHAR(9) NOT NULL,
    city                VARCHAR(30) NOT NULL,
    state               VARCHAR(2) NOT NULL,
    street              VARCHAR(30) NOT NULL,
    number              INTEGER NOT NULL,
    additional_info     VARCHAR(30) NULL,
);

CREATE TYPE request_status AS ENUM('pendente', 'aprovado', 'nao_aprovado');

CREATE TABLE request (
    request_id          SERIAL PRIMARY KEY,
    client_id           INTEGER NOT NULL,
    status              request_status NOT NULL,
    reason_rejection    VARCHAR(255) NULL,
    approved_at         TIMESTAMP NULL
)

