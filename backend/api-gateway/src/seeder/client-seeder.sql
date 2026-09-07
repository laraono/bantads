TRUNCATE TABLE request, state, address, client RESTART IDENTITY CASCADE;

INSERT INTO state (uf) VALUES 
('AC'), ('AL'), ('AP'), ('AM'), ('BA'), ('CE'), ('DF'), ('ES'), ('GO'), ('MA'), 
('MT'), ('MS'), ('MG'), ('PA'), ('PB'), ('PR'), ('PE'), ('PI'), ('RJ'), ('RN'), 
('RS'), ('RO'), ('RR'), ('SC'), ('SP'), ('SE'), ('TO');

INSERT INTO address (
    cep, city, state_id, street, number, additional_info
)
VALUES
('82720-140', 'Curitiba', 16, 'R. Jorn. Ali Chehayde Bark', 421, NULL),
('80730-090', 'Curitiba', 16, 'R. Ferdinando Darif', 203, NULL),
('82010-060', 'Curitiba', 16, 'Rua Luiz Sieracki', 537, NULL),
('82590-300', 'Curitiba', 16, 'R. Rockefeller', 300, NULL),
('82010-280', 'Curitiba', 16, 'R. Simão Lissa', 71, NULL);

INSERT INTO client (
    name, email, cpf, phone, salary, address_id
)
VALUES
('Catharyna', 'cli1@bantads.com.br', '12912861012', '(41) 93355-4817', 10000.0000, 1),
('Cleuddônio', 'cli2@bantads.com.br', '09506382000', '(41)97275-3686', 20000.0000, 2),
('Catianna', 'cli3@bantads.com.br', '85733854057', '(41)92788-1163', 3000.0000, 3),
('Cutardo', 'cli4@bantads.com.br', '58872160006', '(41)92572-2608', 500.0000, 4),
('Coândrya', 'cli5@bantads.com.br', '76179646090', '(41)99844-6486', 1500.0000, 5);
