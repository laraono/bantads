DROP TABLE IF EXISTS manager CASCADE;

CREATE TABLE manager (
    manager_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_manager_cpf UNIQUE (cpf),
    CONSTRAINT uk_manager_email UNIQUE (email)
);

