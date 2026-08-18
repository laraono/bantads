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

INSERT INTO manager (
    name, cpf, email, phone, is_active
)
VALUES
('Geniéve', '98574307084', 'ger1@bantads.com.br', '(41)92194-2818', TRUE),
('Godophredo', '64065268052', 'ger2@bantads.com.br', '(41)96414-7595', TRUE),
('Gyândula', '23862179060', 'ger3@bantads.com.br', '(41)98094-2121', TRUE),
('Gadamântio', '40501740066', 'ger4@bantads.com.br', '(41)93592-7764', TRUE);
