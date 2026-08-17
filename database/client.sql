DROP TABLE IF EXISTS request CASCADE; 
DROP TABLE IF EXISTS client CASCADE; 
DROP TYPE IF EXISTS request_status CASCADE; 

CREATE TYPE request_status AS ENUM ( 
 'pendente', 
 'aprovado', 
 'nao_aprovado' 
); 
CREATE TABLE client ( 
 client_id SERIAL PRIMARY KEY, 
 name VARCHAR(50) NOT NULL, 
 email VARCHAR(100) NOT NULL, 
 cpf VARCHAR(11) NOT NULL, 
 phone VARCHAR(20) NOT NULL, 
 salary NUMERIC(19, 4) NOT NULL, 
 cep VARCHAR(9) NOT NULL, 
 city VARCHAR(30) NOT NULL, 
 state VARCHAR(2) NOT NULL, 
 street VARCHAR(30) NOT NULL, 
 number INTEGER NOT NULL, 
 additional_info VARCHAR(30), 
 CONSTRAINT uk_client_cpf UNIQUE (cpf), 
 CONSTRAINT uk_client_email UNIQUE (email) 
); 
CREATE TABLE request ( 
 request_id SERIAL PRIMARY KEY, 
 client_id INTEGER NOT NULL, 
 status request_status NOT NULL, 
 reason_rejection VARCHAR(255), 
 approved_at TIMESTAMP, 
 CONSTRAINT fk_request_client 
 FOREIGN KEY (client_id) 
 REFERENCES client(client_id) 
); 
INSERT client ( 
 name, email, cpf, phone, salary, cep, city, state, street, number, additional_info 
) 
VALUES 
('Catharyna', 'cli1@bantads.com.br', '12912861012', '(41) 93355-4817', 10000.0000, '82720-140', 'Curitiba', 'PR', 'R. Jorn. Ali Chehayde Bark',421, NULL)('Cleuddônio', 'cli2@bantads.com.br', '09506382000', '(41)97275-3686', 20000.0000, '80730-090', 'Curitiba', 'PR', 'R. Ferdinando Darif',203, NULL('Catianna', 'cli3@bantads.com.br', '85733854057', '(41)92788-1163', 3000.0000, '82010-060', 'Curitiba', 'PR', 'Rua Luiz Sieracki', 537, NULL), ('Cutardo', 'cli4@bantads.com.br', '58872160006', '(41)92572-2608', 500.0000, '82590-300', 'Curitiba', 'PR', 'R. Rockefeller', 300, NULL), ('Coândrya', 'cli5@bantads.com.br', '76179646090', '(41)99844-6486', 1500.0000, 82010-280', 'Curitiba', 'PR', 'R. Simão Lissa', 71, NULL);
